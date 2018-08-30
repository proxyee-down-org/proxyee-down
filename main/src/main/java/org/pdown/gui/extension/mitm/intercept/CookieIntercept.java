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
      httpResponse.headers().set("Content-Type", pipeline.getHttpRequest().headers().get(HttpHeaderNames.COOKIE));
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
