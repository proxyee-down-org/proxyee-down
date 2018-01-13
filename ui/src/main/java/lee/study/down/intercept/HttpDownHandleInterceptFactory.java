package lee.study.down.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.net.InetSocketAddress;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.proxy.ProxyConfig;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HttpDownHandleInterceptFactory implements HttpDownInterceptFactory {

  private int viewPort;

  @Override
  public HttpProxyIntercept create() {
    return new HttpProxyIntercept() {
      @Override
      public void afterResponse(Channel clientChannel, Channel proxyChannel,
          HttpResponse httpResponse,
          HttpProxyInterceptPipeline pipeline) throws Exception {
        HttpRequest httpRequest = pipeline.getHttpRequest();
        TaskInfo taskInfo = HttpDownUtil.getTaskInfo(httpRequest,
            httpResponse.headers(),
            HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup);
        HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, httpRequest,
            ContentManager.CONFIG.get().getSecProxyConfig());
        ContentManager.DOWN.putBoot(httpDownInfo);

        HttpHeaders httpHeaders = httpResponse.headers();
        httpHeaders.clear();
        httpResponse.setStatus(HttpResponseStatus.OK);
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        String host = ((InetSocketAddress) clientChannel.localAddress()).getHostString();
        String js =
            "<script>"
                + "window.top.location.href='http://" + host + ":" + viewPort + "/#/tasks/new/"
                + httpDownInfo
                .getTaskInfo().getId() + "';"
                + "</script>";
        HttpContent content = new DefaultLastHttpContent();
        content.content().writeBytes(js.getBytes());
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, js.getBytes().length);
        clientChannel.writeAndFlush(httpResponse);
        clientChannel.writeAndFlush(content);
        clientChannel.close();
      }
    };
  }
}
