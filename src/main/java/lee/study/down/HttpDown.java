package lee.study.down;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.HttpDownServer;
import lee.study.hanndle.HttpDownInitializer;
import lee.study.model.ChunkInfo;
import lee.study.model.HttpDownInfo;
import lee.study.model.TaskInfo;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpDown {

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
  public static TaskInfo getTaskInfo(HttpRequest httpRequest, HttpHeaders resHeaders,
      NioEventLoopGroup loopGroup) {
    TaskInfo taskInfo = new TaskInfo(
        UUID.randomUUID().toString(), "", getDownFileName(httpRequest, resHeaders), 1,
        getDownFileSize(resHeaders), false, 0, 0, 0, 0, null);
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
                        taskInfo.setSupportRange(true);
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
    return taskInfo;
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
    }
    if (fileName == null) {
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

  /*public static void fastDown(HttpDownInfo downModel, int connections,
      EventLoopGroup LOOP_GROUP, String path, HttpDownCallback callback) throws Exception {
    RequestProto requestProto = ProtoUtil.getRequestProto(downModel.getRequest());
    File file = new File(path + File.separator + downModel.getTaskInfo().getFileName());
    if (file.exists()) {
      file.delete();
    }
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")
    ) {
      randomAccessFile.setLength(downModel.getTaskInfo().getFileSize());
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(LOOP_GROUP) // 注册线程池
          .channel(NioSocketChannel.class); // 使用NioSocketChannel来作为连接用的channel类
      long chunk = downModel.getTaskInfo().getFileSize() / connections;
      AtomicInteger doneConnections = new AtomicInteger(connections);
      AtomicLong fileDownSize = new AtomicLong();
      callback.start(downModel.getTaskInfo());
      for (int i = 0; i < connections; i++) {
        ChannelFuture cf = bootstrap
            .handler(
                new HttpDownInitializer(requestProto.getSsl(), downModel.getTaskInfo(), i, file,
                    doneConnections,
                    fileDownSize, callback))
            .connect(requestProto.getHost(), requestProto.getPort());
        //计算Range
        long start = i * chunk;
        long end = i + 1 == connections ?
            (i + 1) * chunk + downModel.getTaskInfo().getFileSize() % connections - 1
            : (i + 1) * chunk - 1;
        ChunkInfo chunkInfo = new ChunkInfo(UUID.randomUUID().toString(), 0, end - start + 1, 0, 0,
            1);
        callback.chunkStart(downModel.getTaskInfo(), chunkInfo);
        cf.addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            downModel.getRequest().headers()
                .set(HttpHeaderNames.RANGE, "bytes=" + start + "-" + end);
            future.channel().writeAndFlush(downModel.getRequest());
          }
        });
      }
    } catch (Exception e) {
      throw e;
    }

  }*/

  public static void fastDown(HttpDownInfo httpDownInfo, HttpDownCallback callback)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    RequestProto requestProto = ProtoUtil.getRequestProto(httpDownInfo.getRequest());
    File file = new File(taskInfo.getFilePath() + File.separator + taskInfo.getFileName());
    if (file.exists()) {
      file.delete();
    }
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")
    ) {
      randomAccessFile.setLength(taskInfo.getTotalSize());
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(HttpDownServer.LOOP_GROUP) // 注册线程池
          .channel(NioSocketChannel.class); // 使用NioSocketChannel来作为连接用的channel类
      //文件下载开始回调
      taskInfo.setStatus(1);
      taskInfo.setStartTime(System.currentTimeMillis());
      callback.start(taskInfo);
      for (int i = 0; i < taskInfo.getConnections(); i++) {
        ChunkInfo chunkInfo = taskInfo.getChunkInfoList().get(i);
        ChannelFuture cf = bootstrap
            .handler(
                new HttpDownInitializer(requestProto.getSsl(), taskInfo, chunkInfo, callback))
            .connect(requestProto.getHost(), requestProto.getPort());
        //分段下载开始回调
        chunkInfo.setStatus(1);
        chunkInfo.setStartTime(System.currentTimeMillis());
        callback.chunkStart(taskInfo, chunkInfo);
        cf.addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            httpDownInfo.getRequest().headers()
                .set(HttpHeaderNames.RANGE,
                    "bytes=" + chunkInfo.getStartPosition() + "-" + chunkInfo.getEndPosition());
            future.channel().writeAndFlush(httpDownInfo.getRequest());
          }
        });
      }
    } catch (Exception e) {
      throw e;
    }

  }
}
