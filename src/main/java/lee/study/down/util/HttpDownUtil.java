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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.down.HttpDownServer;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.hanndle.HttpDownInitializer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.model.HttpRequestInfo;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpDownUtil {

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
    /*HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo,
        HttpRequestInfo.adapter(httpRequest));*/
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, httpRequest);
    HttpDownServer.DOWN_CONTENT.put(taskInfo.getId(), httpDownInfo);
    httpHeaders.clear();
    httpResponse.setStatus(HttpResponseStatus.OK);
    httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    String js =
        "<script>window.top.location.href='http://localhost:" + HttpDownServer.VIEW_SERVER_PORT
            + "/#/newTask/" + httpDownInfo
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
      NioEventLoopGroup loopGroup) {
    TaskInfo taskInfo = new TaskInfo(
        UUID.randomUUID().toString(), "", getDownFileName(httpRequest, resHeaders), 1,
        getDownFileSize(resHeaders), false, 0, 0, 0, 0);
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
                      ctx0.channel().close();
                      cdl.countDown();
                    }
                  }
                });
              }

            });
        ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort()).sync();
        //请求下载一个字节测试是否支持断点下载
        HttpRequestInfo requestInfo = (HttpRequestInfo) httpRequest;
        httpRequest.headers().set(HttpHeaderNames.RANGE, "bytes=0-0");
        cf.channel().writeAndFlush(httpRequest);
        if (requestInfo.content() != null) {
          //请求体写入
          HttpContent content = new DefaultLastHttpContent();
          content.content().writeBytes(requestInfo.content());
          cf.channel().writeAndFlush(content);
        }
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
    String contentLength = resHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
    if (contentLength != null) {
      return Long.valueOf(resHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
    } else {
      return -1;
    }
  }

  public static void taskDown(HttpDownInfo httpDownInfo, HttpDownCallback callback)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    taskInfo.setCallback(callback);
    RequestProto requestProto = ProtoUtil.getRequestProto(httpDownInfo.getRequest());
    File file = new File(taskInfo.getFilePath() + File.separator + taskInfo.getFileName());
    if (file.exists()) {
      file.delete();
    }
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")
    ) {
      if (taskInfo.getTotalSize() > 0) {
        randomAccessFile.setLength(taskInfo.getTotalSize());
      }
      //文件下载开始回调
      taskInfo.setStatus(1);
      taskInfo.setStartTime(System.currentTimeMillis());
      callback.start(taskInfo);
      for (int i = 0; i < taskInfo.getChunkInfoList().size(); i++) {
        chunkDown(httpDownInfo, taskInfo.getChunkInfoList().get(i), requestProto);
        /*ChunkInfo chunkInfo = taskInfo.getChunkInfoList().get(i);
        ChannelFuture cf = HttpDownServer.DOWN_BOOT
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
        });*/
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public static void chunkDown(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo,
      RequestProto requestProto)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    HttpDownCallback callback = taskInfo.getCallback();
    ChannelFuture cf = HttpDownServer.DOWN_BOOT
        .handler(
            new HttpDownInitializer(requestProto.getSsl(), taskInfo, chunkInfo, callback))
        .connect(requestProto.getHost(), requestProto.getPort());
    cf.addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        HttpRequestInfo requestInfo = (HttpRequestInfo) httpDownInfo.getRequest();
        if (httpDownInfo.getTaskInfo().isSupportRange()) {
          requestInfo.headers()
              .set(HttpHeaderNames.RANGE,
                  "bytes=" + chunkInfo.getNowStartPosition() + "-" + chunkInfo.getEndPosition());
        }
        future.channel().writeAndFlush(httpDownInfo.getRequest());
        if (requestInfo.content() != null) {
          //请求体写入
          HttpContent content = new DefaultLastHttpContent();
          content.content().writeBytes(requestInfo.content());
          future.channel().writeAndFlush(content);
        }
      } else {
        //失败等30s重试
        TimeUnit.SECONDS.sleep(30);
        retryDown(taskInfo, chunkInfo);
      }
    });
  }

  /**
   * 下载重试
   */
  public static void retryDown(TaskInfo taskInfo, ChunkInfo chunkInfo)
      throws Exception {
    safeClose(chunkInfo.getChannel(), chunkInfo.getFileChannel());
    if (taskInfo.isSupportRange()) {
      chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
    }
    HttpDownInfo httpDownInfo = HttpDownServer.DOWN_CONTENT.get(taskInfo.getId());
    RequestProto requestProto = ProtoUtil
        .getRequestProto(httpDownInfo.getRequest());
    chunkDown(httpDownInfo, chunkInfo, requestProto);
  }

  public static void safeClose(Channel channel, FileChannel fileChannel) {
    try {
      if (channel != null && channel.isOpen()) {
        //关闭旧的下载连接
        channel.close();
      }
      if (fileChannel != null && fileChannel.isOpen()) {
        //关闭旧的下载文件连接
        fileChannel.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
