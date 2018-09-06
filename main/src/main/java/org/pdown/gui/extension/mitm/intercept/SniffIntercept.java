package org.pdown.gui.extension.mitm.intercept;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.net.URLEncoder;
import java.util.Arrays;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.core.util.ProtoUtil.RequestProto;
import org.pdown.gui.DownApplication;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.rest.form.HttpRequestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SniffIntercept extends HttpProxyIntercept {

  private final static Logger LOGGER = LoggerFactory.getLogger(SniffIntercept.class);

  private boolean matchFlag = false;

  private ByteBuf content;
  private boolean downFlag = false;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    matchFlag = ExtensionContent.getSniffRegexs().stream().anyMatch(regex -> HttpUtil.checkUrl(httpRequest, regex));
    if (!matchFlag) {
      super.beforeRequest(clientChannel, httpRequest, pipeline);
      return;
    }
    String contentLength = httpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH);
    //缓存request content
    if (contentLength != null) {
      content = PooledByteBufAllocator.DEFAULT.buffer();
    }
    pipeline.beforeRequest(clientChannel, HttpRequestInfo.adapter(httpRequest));
  }

  @Override
  public void beforeRequest(Channel clientChannel, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!matchFlag) {
      super.beforeRequest(clientChannel, httpContent, pipeline);
      return;
    }
    if (content != null) {
      ByteBuf temp = httpContent.content().slice();
      content.writeBytes(temp);
      if (httpContent instanceof LastHttpContent) {
        try {
          byte[] contentBts = new byte[content.readableBytes()];
          content.readBytes(contentBts);
          ((HttpRequestInfo) pipeline.getHttpRequest()).setContent(contentBts);
        } finally {
          ReferenceCountUtil.release(content);
        }
      }
    }
    pipeline.beforeRequest(clientChannel, httpContent);
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!matchFlag) {
      super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
      return;
    }
    if ((httpResponse.status().code() + "").indexOf("20") == 0) { //响应码为20x
      HttpHeaders httpResHeaders = httpResponse.headers();
      String accept = pipeline.getHttpRequest().headers().get(HttpHeaderNames.ACCEPT);
      String contentType = httpResHeaders.get(HttpHeaderNames.CONTENT_TYPE);
      //有两种情况进行下载 1.url后缀为.xxx  2.带有CONTENT_DISPOSITION:ATTACHMENT响应头
      String disposition = httpResHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
      if (accept != null
          && accept.matches("^.*text/html.*$")
          && ((disposition != null
          && disposition.contains(HttpHeaderValues.ATTACHMENT)
          && disposition.contains(HttpHeaderValues.FILENAME))
          || isDownContentType(contentType))) {
        downFlag = true;
      }

      HttpRequestInfo httpRequestInfo = (HttpRequestInfo) pipeline.getHttpRequest();
      if (downFlag) {   //如果是下载
        LOGGER.debug("=====================下载===========================\n" +
            pipeline.getHttpRequest().toString() + "\n" +
            "------------------------------------------------" +
            httpResponse.toString() + "\n" +
            "================================================");
        proxyChannel.close();//关闭嗅探下载连接
        httpRequestInfo.setRequestProto(new RequestProto(pipeline.getRequestProto().getHost(), pipeline.getRequestProto().getPort(), pipeline.getRequestProto().getSsl()));
        HttpRequestForm requestForm = HttpRequestForm.parse(httpRequestInfo);
        HttpResponseInfo responseInfo = HttpDownUtil.getHttpResponseInfo(httpRequestInfo, null, null, (NioEventLoopGroup) clientChannel.eventLoop().parent());
        responseInfo.setSupportRange(false);
        httpResponse.headers().clear();
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        String js = "<script type=\"text/javascript\">window.history.go(-1)</script>";
        HttpContent httpContent = new DefaultLastHttpContent();
        httpContent.content().writeBytes(js.getBytes());
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpContent.content().readableBytes());
        clientChannel.writeAndFlush(httpResponse);
        clientChannel.writeAndFlush(httpContent);
        clientChannel.close();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestParam = URLEncoder.encode(objectMapper.writeValueAsString(requestForm), "utf-8");
        String responseParam = URLEncoder.encode(objectMapper.writeValueAsString(responseInfo), "utf-8");
        String uri = "/#/tasks?request=" + requestParam + "&response=" + responseParam;
        DownApplication.INSTANCE.loadUri(uri, false);
        return;
      } else {
        if (httpRequestInfo.content() != null) {
          httpRequestInfo.setContent(null);
        }
      }
    }
    super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!matchFlag) {
      super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
      return;
    }
    if (downFlag) {
      httpContent.release();
    } else {
      pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
    }
  }

  //https://chromium.googlesource.com/chromium/src/+/master/net/base/mime_util.cc
  private static final String[] CONTENT_TYPES = {
      "application/javascript",
      "application/x-javascript",
      "application/wasm",
      "application/x-chrome-extension",
      "application/xhtml+xml",
      "application/font-woff",
      "application/json",
      "application/x-shockwave-flash",
      "audio/mpeg",
      "audio/flac",
      "audio/mp3",
      "audio/ogg",
      "audio/wav",
      "audio/webm",
      "audio/x-m4a",
      "image/gif",
      "image/jpeg",
      "image/png",
      "image/apng",
      "image/webp",
      "image/x-icon",
      "image/bmp",
      "image/jpeg",
      "image/svg+xml",
      "image/tiff",
      "image/vnd.microsoft.icon",
      "image/x-png",
      "image/x-xbitmap",
      "video/webm",
      "video/ogg",
      "video/mp4",
      "video/mpeg",
      "text/css",
      "text/html",
      "text/xml",
      "text/calendar",
      "text/html",
      "text/plain",
      "text/x-sh",
      "text/xml",
      "multipart/related",
      "message/rfc822",
  };

  private boolean isDownContentType(String contentType) {
    if (contentType != null) {
      String contentTypeFinal = contentType.split(";")[0].trim().toLowerCase();
      return Arrays.stream(CONTENT_TYPES).noneMatch(type -> contentTypeFinal.equals(type));
    }
    return true;
  }
}
