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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.down.io.LargeMappedByteBuffer;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpDownUtil {

  /**
   * 检测是否支持断点下载
   */
  public static TaskInfo getTaskInfo(HttpRequest httpRequest, HttpHeaders resHeaders,
      SslContext clientSslCtx, NioEventLoopGroup loopGroup)
      throws Exception {
    HttpResponse httpResponse = null;
    if (resHeaders == null) {
      httpResponse = getResponse(httpRequest, clientSslCtx, loopGroup);
      //处理重定向
      if ((httpResponse.status().code() + "").indexOf("30") == 0) {
        String redirectUrl = httpResponse.headers().get(HttpHeaderNames.LOCATION);
        HttpRequestInfo requestInfo = (HttpRequestInfo) httpRequest;
        requestInfo.headers().remove("Host");
        requestInfo.setUri(redirectUrl);
        RequestProto requestProto = ProtoUtil.getRequestProto(requestInfo);
        requestInfo.headers().set("Host", requestProto.getHost());
        requestInfo.setRequestProto(requestProto);
        httpResponse = getResponse(httpRequest, clientSslCtx, loopGroup);
      }
      resHeaders = httpResponse.headers();
    }
    TaskInfo taskInfo = new TaskInfo()
        .setId(UUID.randomUUID().toString())
        .setFileName(getDownFileName(httpRequest, resHeaders))
        .setTotalSize(getDownFileTotalSize(resHeaders));
    //chunked编码不支持断点下载
    if (resHeaders.contains(HttpHeaderNames.CONTENT_LENGTH)) {
      if (httpResponse == null) {
        httpResponse = getResponse(httpRequest, clientSslCtx, loopGroup);
      }
      //206表示支持断点下载
      if (httpResponse.status().equals(HttpResponseStatus.PARTIAL_CONTENT)) {
        taskInfo.setSupportRange(true);
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
   * 取当前请求下载文件的总大小
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

  /**
   * 取请求下载文件的总大小
   */
  public static long getDownFileTotalSize(HttpHeaders resHeaders) {
    String contentRange = resHeaders.get(HttpHeaderNames.CONTENT_RANGE);
    if (contentRange != null) {
      Pattern pattern = Pattern.compile("^.*/(\\d+).*$");
      Matcher matcher = pattern.matcher(contentRange);
      if (matcher.find()) {
        return Long.parseLong(matcher.group(1));
      }
    } else {
      String contentLength = resHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
      if (contentLength != null) {
        return Long.valueOf(resHeaders.get(HttpHeaderNames.CONTENT_LENGTH));
      }
    }
    return 0;
  }

  /**
   * 取请求响应
   */
  public static HttpResponse getResponse(HttpRequest httpRequest, SslContext clientSslCtx,
      NioEventLoopGroup loopGroup) throws Exception {
    final HttpResponse[] httpResponses = new HttpResponse[1];
    CountDownLatch cdl = new CountDownLatch(1);
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
                  httpResponses[0] = httpResponse;
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
    if (httpResponses[0] == null) {
      throw new TimeoutException("getResponse timeout");
    }
    return httpResponses[0];
  }

  public static void safeClose(Channel channel, FileChannel fileChannel,
      LargeMappedByteBuffer mappedBuffer) throws IOException {
    if (channel != null) {
      //关闭旧的下载连接
      channel.close();
    }
    if (fileChannel != null) {
      //关闭旧的下载文件连接
      fileChannel.close();
    }
    if (mappedBuffer != null) {
      //关闭旧的下载文件连接
      mappedBuffer.close();
    }
  }
}
