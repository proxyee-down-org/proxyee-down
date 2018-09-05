package org.pdown.gui.extension.mitm.intercept;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.internal.StringUtil;
import java.net.URL;

/**
 * 嗅探目标网站cookie，支持HTTP only
 */
public class CookieIntercept extends HttpProxyIntercept {

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
    String acceptValue = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
    if (acceptValue != null && acceptValue.contains("application/x-sniff-cookie")) {
      HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, new DefaultHttpHeaders());
      httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
      //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
      AsciiString customHeadKey = AsciiString.cached("X-Sniff-Cookie");
      httpResponse.headers().set(customHeadKey, pipeline.getHttpRequest().headers().get(HttpHeaderNames.COOKIE));
      httpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS, customHeadKey);
      String origin = httpRequest.headers().get(HttpHeaderNames.ORIGIN);
      if (StringUtil.isNullOrEmpty(origin)) {
        String referer = httpRequest.headers().get(HttpHeaderNames.REFERER);
        URL url = new URL(referer);
        origin = url.getHost();
      }
      httpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      httpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
      clientChannel.writeAndFlush(httpResponse);
      clientChannel.writeAndFlush(new DefaultLastHttpContent());
      clientChannel.close();
    } else {
      super.beforeRequest(clientChannel, httpRequest, pipeline);
    }
  }
}
