package org.pdown.gui.extension.mitm.intercept;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;

/**
 * 嗅探目标网站cookie，支持HTTP only
 */
public class CookieIntercept extends HttpProxyIntercept {

  private boolean sniffFlag;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
    String acceptValue = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
    if (acceptValue != null && acceptValue.contains("application/x-sniff-cookie")) {
      sniffFlag = true;
    }
    super.beforeRequest(clientChannel, httpRequest, pipeline);
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
    if (sniffFlag) {
      httpResponse.setStatus(HttpResponseStatus.OK);
      httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
      //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
      AsciiString customHeadKey = AsciiString.cached("X-Sniff-Cookie");
      httpResponse.headers().set(customHeadKey, pipeline.getHttpRequest().headers().get(HttpHeaderNames.COOKIE));
      httpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS, customHeadKey);
      proxyChannel.close();
      super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
      clientChannel.write(new DefaultLastHttpContent());
    } else {
      super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!sniffFlag) {
      super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }
  }

}
