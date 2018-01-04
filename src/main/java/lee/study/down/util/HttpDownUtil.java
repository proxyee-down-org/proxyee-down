package lee.study.down.util;

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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.down.HttpDownServer;
import lee.study.down.hanndle.HttpDownInitializer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpDownUtil {

//  private static final RecvByteBufAllocator RECV_BYTE_BUF_ALLOCATOR = new AdaptiveRecvByteBufAllocator(64,8192,65536);
  public static final AttributeKey<Boolean> CLOSE_ATTR = AttributeKey.newInstance("close");

  /**
   * 检测请求头是否存在
   */
  public static boolean checkHeadKey(HttpHeaders httpHeaders, String regex) {
    for (Entry<String, String> entry : httpHeaders) {
      if (entry.getKey().matches(regex)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检测url是否匹配
   */
  public static boolean checkUrl(HttpRequest httpRequest, String regex) {
    return checkHead(httpRequest, HttpHeaderNames.HOST, regex);
  }

  /**
   * 检测Referer是否匹配
   */
  public static boolean checkReferer(HttpRequest httpRequest, String regex) {
    return checkHead(httpRequest, HttpHeaderNames.REFERER, regex);
  }

  /**
   * 检测某个http头是否匹配
   */
  public static boolean checkHead(HttpRequest httpRequest, CharSequence headName, String regex) {
    String host = httpRequest.headers().get(headName);
    if (host != null && regex != null) {
      String url;
      if (httpRequest.uri().indexOf("/") == 0) {
        url = host + httpRequest.uri();
      } else {
        url = httpRequest.uri();
      }
      return url.matches(regex);
    }
    return false;
  }

  public static void startDownTask(TaskInfo taskInfo, HttpRequest httpRequest,
      HttpResponse httpResponse, Channel clientChannel) {
    HttpHeaders httpHeaders = httpResponse.headers();
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, httpRequest);
    HttpDownServer.DOWN_CONTENT.put(taskInfo.getId(), httpDownInfo);
    httpHeaders.clear();
    httpResponse.setStatus(HttpResponseStatus.OK);
    httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    String host = ((InetSocketAddress) clientChannel.localAddress()).getHostString();
    String js =
        "<script>window.top.location.href='http://" + host + ":" + HttpDownServer.VIEW_SERVER_PORT
            + "/#/tasks/new/" + httpDownInfo
            .getTaskInfo().getId()
            + "';</script>";
    HttpContent content = new DefaultLastHttpContent();
    content.content().writeBytes(js.getBytes());
    httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, js.getBytes().length);
    clientChannel.writeAndFlush(httpResponse);
    clientChannel.writeAndFlush(content);
    clientChannel.close();
  }

  /**
   * 检测是否支持断点下载
   */
  public static TaskInfo getTaskInfo(HttpRequest httpRequest, HttpHeaders resHeaders,
      SslContext clientSslCtx, NioEventLoopGroup loopGroup) {
    TaskInfo taskInfo = new TaskInfo()
        .setId(UUID.randomUUID().toString())
        .setFileName(getDownFileName(httpRequest, resHeaders))
        .setTotalSize(getDownFileSize(resHeaders));
    //chunked编码不支持断点下载
    if (resHeaders.contains(HttpHeaderNames.CONTENT_LENGTH)) {
      CountDownLatch cdl = new CountDownLatch(1);
      try {
        HttpRequestInfo requestInfo = (HttpRequestInfo) httpRequest;
        RequestProto requestProto = requestInfo.requestProto();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup) // 注册线程池
            .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
            .handler(new ChannelInitializer() {

              @Override
              protected void initChannel(Channel ch) throws Exception {
                if (requestProto.getSsl()) {
                  ch.pipeline().addLast(clientSslCtx.newHandler(ch.alloc()));
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
                      ctx0.channel().close();
                      cdl.countDown();
                    }
                  }
                });
              }

            });
        ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
        cf.addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            //请求下载一个字节测试是否支持断点下载
            httpRequest.headers().set(HttpHeaderNames.RANGE, "bytes=0-0");
            cf.channel().writeAndFlush(httpRequest);
            if (requestInfo.content() != null) {
              //请求体写入
              HttpContent content = new DefaultLastHttpContent();
              content.content().writeBytes(requestInfo.content());
              cf.channel().writeAndFlush(content);
            }
          } else {
            cdl.countDown();
          }
        });
        cdl.await(30, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        HttpDownServer.LOGGER.error("await:", e);
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
        try {
          fileName = new String(bts, "UTF-8");
          fileName = URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          fileName = null;
        }
      }
    }
    if (fileName == null) {
      Pattern pattern = Pattern.compile("^.*/([^/]*\\.[^./]{1,5})(\\?[^?]*)?$");
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
    String contentRange = resHeaders.get(HttpHeaderNames.CONTENT_RANGE);
    if (contentRange != null) {
      Pattern pattern = Pattern.compile("^[^\\d]*(\\d+)-(\\d+)/.*$");
      Matcher matcher = pattern.matcher(contentRange);
      if (matcher.find()) {
        long startSize = Long.parseLong(matcher.group(1));
        long endSize = Long.parseLong(matcher.group(2));
        return endSize - startSize + 1;
      }
    } else {
      String contentLength = resHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
      if (contentLength != null) {
        return Long.valueOf(resHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
      }
    }
    return 0;
  }

  public static void taskDown(HttpDownInfo httpDownInfo)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    try {
      FileUtil.deleteIfExists(taskInfo.buildTaskFilePath());
      try (
          RandomAccessFile randomAccessFile = new RandomAccessFile(taskInfo.buildTaskFilePath(),
              "rw")
      ) {
        randomAccessFile.setLength(taskInfo.getTotalSize());
      }
      //文件下载开始回调
      taskInfo.setStatus(1);
      taskInfo.setStartTime(System.currentTimeMillis());
      HttpDownServer.CALLBACK.onStart(taskInfo);
      for (int i = 0; i < taskInfo.getChunkInfoList().size(); i++) {
        ChunkInfo chunkInfo = taskInfo.getChunkInfoList().get(i);
        //避免分段下载速度比总的下载速度大太多的问题
        chunkInfo.setStatus(1);
        chunkInfo.setStartTime(taskInfo.getStartTime());
        chunkDown(httpDownInfo, chunkInfo);
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public static void chunkDown(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    HttpRequestInfo requestInfo = (HttpRequestInfo) httpDownInfo.getRequest();
    RequestProto requestProto = requestInfo.requestProto();
    HttpDownServer.LOGGER.debug(
        "开始下载：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
    ChannelFuture cf = HttpDownServer.DOWN_BOOT
//        .option(ChannelOption.RCVBUF_ALLOCATOR,RECV_BYTE_BUF_ALLOCATOR)
        .handler(
            new HttpDownInitializer(requestProto.getSsl(), taskInfo, chunkInfo,
                HttpDownServer.CALLBACK))
        .connect(requestProto.getHost(), requestProto.getPort());
    cf.addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        synchronized (requestInfo) {
          HttpDownServer.LOGGER.debug(
              "下载连接成功：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
          if (httpDownInfo.getTaskInfo().isSupportRange()) {
            requestInfo.headers()
                .set(HttpHeaderNames.RANGE,
                    "bytes=" + chunkInfo.getNowStartPosition() + "-" + chunkInfo.getEndPosition());
          } else {
            requestInfo.headers().remove(HttpHeaderNames.RANGE);
          }
          future.channel().writeAndFlush(httpDownInfo.getRequest());
        }
        if (requestInfo.content() != null) {
          //请求体写入
          HttpContent content = new DefaultLastHttpContent();
          content.content().writeBytes(requestInfo.content());
          future.channel().writeAndFlush(content);
          requestInfo.setContent(null); //help GC
        }
      } else {
        HttpDownServer.LOGGER.debug(
            "下载连接失败：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
        future.channel().close();
      }
    });
  }

  /**
   * 下载重试
   */
  public static void retryDown(TaskInfo taskInfo, ChunkInfo chunkInfo)
      throws Exception {
    retryDown(taskInfo, chunkInfo, -1);
  }

  /**
   * 下载重试
   */
  public static void retryDown(TaskInfo taskInfo, ChunkInfo chunkInfo, long downSize)
      throws Exception {
    synchronized (chunkInfo) {
      safeClose(chunkInfo.getChannel(), chunkInfo);
      if (setStatusIfNotDone(chunkInfo, 3)) {
        if (downSize != -1) {
          chunkInfo.setDownSize(downSize);
        }
        //已经下载完成
        if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
          chunkInfo.setStatus(2);
          HttpDownServer.CALLBACK.onChunkDone(taskInfo, chunkInfo);
          return;
        }
        if (taskInfo.isSupportRange()) {
          chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
        }
        HttpDownInfo httpDownInfo = HttpDownServer.DOWN_CONTENT.get(taskInfo.getId());
        chunkDown(httpDownInfo, chunkInfo);
      }
    }
  }

  /**
   * 继续下载
   */
  public static void continueDown(TaskInfo taskInfo, ChunkInfo chunkInfo)
      throws Exception {
    synchronized (chunkInfo) {
      safeClose(chunkInfo.getChannel(), chunkInfo);
      //避免同时两个重新下载
      if (setStatusIfNotDone(chunkInfo, 5)) {
        //计算后续下载字节
        chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
        HttpDownInfo httpDownInfo = HttpDownServer.DOWN_CONTENT.get(taskInfo.getId());
        chunkDown(httpDownInfo, chunkInfo);
      }
    }
  }

  public static void safeClose(Channel channel, ChunkInfo chunkInfo) {
    try {
      if (channel != null ) {
        channel.attr(CLOSE_ATTR).set(true);
        if(channel.isOpen()){
          //关闭旧的下载连接
          channel.close();
        }
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("safeClose netty channel", e);
    }
    try {
      FileChannel fileChannel = chunkInfo.getFileChannel();
      if (fileChannel != null && fileChannel.isOpen()) {
        //关闭旧的下载文件连接
        fileChannel.close();
      }
    } catch (IOException e) {
      HttpDownServer.LOGGER.error("safeClose file channel", e);
    }
    try {
      MappedByteBuffer mappedBuffer = chunkInfo.getMappedBuffer();
      if (mappedBuffer != null) {
        //关闭旧的下载文件连接
        FileUtil.unmap(mappedBuffer);
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("safeClose file mappedBuffer", e);
    }
  }

  public static boolean setStatusIfNotDone(ChunkInfo chunkInfo, int update) {
    if (chunkInfo.getStatus() != 2) {
      chunkInfo.setStatus(update);
      return true;
    }
    return false;
  }
}
