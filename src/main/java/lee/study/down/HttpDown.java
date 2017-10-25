package lee.study.down;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lee.study.hanndle.HttpDownInitializer;
import lee.study.proxyee.NettyHttpProxyServer;
import lee.study.proxyee.util.ProtoUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpDown {

    public static class DownInfo {
        private String fileName;
        private long fileSize;
        private boolean supportRange;

        public DownInfo(String fileName, long fileSize, boolean supportRange) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.supportRange = supportRange;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public boolean getSupportRange() {
            return supportRange;
        }

        public void setSupportRange(boolean supportRange) {
            this.supportRange = supportRange;
        }
    }

    public static void download(HttpRequest httpRequest, HttpHeaders resHeaders, NioEventLoopGroup loopGroup) {
        DownInfo downInfo = getDownInfo(httpRequest,resHeaders,loopGroup);
        String fileName = downInfo.getFileName();
        System.out.println("下载请求：" + httpRequest.uri());
        System.out.println("文件名：" + fileName);
        System.out.println("文件大小：" + (downInfo.getFileSize()==-1?"未知":downInfo.getFileSize()+"byte"));
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入下载目录：");
        String dir = scanner.next();
        int connections = 1;
        if(downInfo.getSupportRange()){
            System.out.print("输入下载连接数：");
            connections = scanner.nextInt();
        }
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            System.out.println("目录不存在！");
        } else {
            File file = new File(dirFile.getPath() + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                final ProtoUtil.RequestProto requestProto = ProtoUtil.getRequestProto(httpRequest);
                List<File> chunkFileList = Collections.synchronizedList(new ArrayList<>());
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(loopGroup) // 注册线程池
                        .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                        .handler(new HttpDownInitializer(file,requestProto.getSsl(),connections,chunkFileList));
                if(downInfo.getSupportRange()){
                    //多连接下载
                    long chunk = downInfo.getFileSize()/connections;
                    for (int i =0;i<connections;i++){
                        final long start = i*chunk;
                        final long end = i+1==connections?(i+1)*(chunk-1)+downInfo.getFileSize()%connections:(i+1)*chunk-1;
                        ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
                        cf.addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                //计算起始和开始位置
                                System.out.println("开始下载：bytes="+start+"-"+end);
                                synchronized (httpRequest){
                                    httpRequest.headers().set(HttpHeaderNames.RANGE, "bytes="+start+"-"+end);
                                    future.channel().writeAndFlush(httpRequest);
                                }
                            }
                        });
                    }
                }else{
                    ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort()).sync();
                    cf.channel().writeAndFlush(httpRequest);
                }
            } catch (Exception e) {
                System.out.println("下载失败");
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException {
        long fileSize = 106;
        int connections = 1;
        long chunk = fileSize/connections;
        for (int i =0;i<connections;i++){
            long start = i*chunk;
            long end = (i+1)*chunk-1;
            if(i+1==connections){
                end+=fileSize%connections;
            }
            System.out.println("bytes="+start+"-"+end);
        }
    }

    /**
     * 检测是否支持断点下载
     */
    public static DownInfo getDownInfo(HttpRequest httpRequest, HttpHeaders resHeaders, NioEventLoopGroup loopGroup) {
        DownInfo downInfo = new DownInfo(getDownFileName(httpRequest,resHeaders), getDownFileSize(resHeaders), false);
        //chunked编码不支持断点下载
        if (resHeaders.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            CountDownLatch cdl = new CountDownLatch(1);
            try {
                final ProtoUtil.RequestProto requestProto = ProtoUtil.getRequestProto(httpRequest);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(loopGroup) // 注册线程池
                        .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                        .handler(new ChannelInitializer() {

                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                if (requestProto.getSsl()) {
                                    ch.pipeline().addLast(NettyHttpProxyServer.clientSslCtx.newHandler(ch.alloc()));
                                }
                                ch.pipeline().addLast("httpCodec", new HttpClientCodec());
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx0, Object msg0) throws Exception {
                                        if (msg0 instanceof HttpResponse) {
                                            HttpResponse httpResponse = (HttpResponse) msg0;
                                            System.out.println(httpResponse.toString());
                                            //206表示支持断点下载
                                            if (httpResponse.status().equals(HttpResponseStatus.PARTIAL_CONTENT)) {
                                                downInfo.setSupportRange(true);
                                            }
                                            cdl.countDown();
                                        } else if (msg0 instanceof DefaultLastHttpContent) {
                                            ctx0.channel().close();
                                        }
                                    }
                                });
                            }

                        });
                ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort()).sync();
                //请求下载一个字节测试是否支持断点下载
                httpRequest.headers().set(HttpHeaderNames.RANGE, "bytes=0-0");
                cf.channel().writeAndFlush(httpRequest);
                cdl.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return downInfo;
    }

    public static String getDownFileName(HttpRequest httpRequest, HttpHeaders resHeaders) {
        String fileName = null;
        String disposition = resHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
        if (disposition != null) {
            //attachment;filename=1.rar   attachment;filename=*UTF-8''1.rar
            Pattern pattern = Pattern.compile("^.*filename\\*?=\"?(?:.*'')?([^\"]*)\"?$");
            Matcher matcher = pattern.matcher(disposition);
            if (matcher.find()) {
                char[] chs = matcher.group(1).toCharArray();
                byte[] bts = new byte[chs.length];
                //netty将byte转成了char，导致中文乱码 HttpObjectDecoder(:803)
                for (int i = 0; i < chs.length; i++) {
                    bts[i] = (byte) chs[i];
                }
                fileName = new String(bts);
                try {
                    fileName = URLDecoder.decode(fileName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    fileName = null;
                }
            }
        } else {
            Pattern pattern = Pattern.compile("^.*/([^/]*)$");
            Matcher matcher = pattern.matcher(httpRequest.uri());
            if (matcher.find()) {
                fileName = matcher.group(1);
            }
        }
        return fileName == null ? "未知文件.xxx" : fileName;
    }

    /**
     * 取下载文件的总大小
     * @param resHeaders
     * @return
     */
    public static long getDownFileSize(HttpHeaders resHeaders){
        String contentLength = resHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
        if(contentLength!=null){
            return Long.valueOf(resHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
        }else{
            return -1;
        }
    }

}
