package lee.study.down;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.hanndle.HttpDownInitializer;
import lee.study.model.HttpDownModel;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

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

  public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException {
    long fileSize = 106;
    int connections = 1;
    long chunk = fileSize / connections;
    for (int i = 0; i < connections; i++) {
      long start = i * chunk;
      long end = (i + 1) * chunk - 1;
      if (i + 1 == connections) {
        end += fileSize % connections;
      }
      System.out.println("bytes=" + start + "-" + end);
    }
  }

  /**
   * 检测是否支持断点下载
   */
  public static DownInfo getDownInfo(HttpRequest httpRequest, HttpHeaders resHeaders,
      NioEventLoopGroup loopGroup) {
    DownInfo downInfo = new DownInfo(getDownFileName(httpRequest, resHeaders),
        getDownFileSize(resHeaders), false);
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
                  ch.pipeline().addLast(HttpProxyServer.clientSslCtx.newHandler(ch.alloc()));
                }
                ch.pipeline().addLast("httpCodec", new HttpClientCodec());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                  @Override
                  public void channelRead(ChannelHandlerContext ctx0, Object msg0)
                      throws Exception {
                    if (msg0 instanceof HttpResponse) {
                      HttpResponse httpResponse = (HttpResponse) msg0;
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
   */
  public static long getDownFileSize(HttpHeaders resHeaders) {
    String contentLength = resHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
    if (contentLength != null) {
      return Long.valueOf(resHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
    } else {
      return -1;
    }
  }

  public static void fastDown(HttpDownModel downModel, int connections,
      EventLoopGroup loopGroup, String path, HttpDownCallback callback) throws Exception {
    RequestProto requestProto = ProtoUtil.getRequestProto(downModel.getRequest());
    File file = new File(path + "/" + downModel.getDownInfo().getFileName());
    if (file.exists()) {
      file.delete();
    }
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")
    ) {
      randomAccessFile.setLength(downModel.getDownInfo().fileSize);
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(loopGroup) // 注册线程池
          .channel(NioSocketChannel.class); // 使用NioSocketChannel来作为连接用的channel类
      long chunk = downModel.getDownInfo().getFileSize() / connections;
      AtomicInteger doneConnections = new AtomicInteger(connections);
      AtomicLong fileDownSize = new AtomicLong();
      for (int i = 0; i < connections; i++) {
        ChannelFuture cf = bootstrap
            .handler(new HttpDownInitializer(requestProto.getSsl(), i, file, doneConnections,
                fileDownSize, callback))
            .connect(requestProto.getHost(), requestProto.getPort());
        int finalI = i;
        cf.addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            int index = finalI;
            //计算起始和开始位置
            long start = index * chunk;
            long end = index + 1 == connections ?
                (index + 1) * chunk + downModel.getDownInfo().getFileSize() % connections - 1
                : (index + 1) * chunk - 1;
            downModel.getRequest().headers()
                .set(HttpHeaderNames.RANGE, "bytes=" + start + "-" + end);
            future.channel().writeAndFlush(downModel.getRequest());
          }
        });
      }
    } catch (Exception e) {
      throw e;
    }

  }

}
