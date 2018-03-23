package lee.study.down.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.HttpDownUtil;
import lee.study.down.util.HttpUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.proxy.ProxyConfig;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HttpDownHandleInterceptFactory implements HttpDownInterceptFactory {

  private HttpDownDispatch httpDownDispatch;

  @Override
  public HttpProxyIntercept create() {
    return new HttpProxyIntercept() {

      @Override
      public void afterResponse(Channel clientChannel, Channel proxyChannel,
          HttpResponse httpResponse,
          HttpProxyInterceptPipeline pipeline) throws Exception {
        HttpRequest httpRequest = pipeline.getHttpRequest();
        ProxyConfig proxyConfig = ContentManager.CONFIG.get().getSecProxyConfig();
        TaskInfo taskInfo = HttpDownUtil.getTaskInfo(httpRequest,
            httpResponse.headers(),
            proxyConfig,
            HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup);
        HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, httpRequest, proxyConfig);
        ContentManager.DOWN.putBoot(httpDownInfo);
        httpResponse.setStatus(HttpResponseStatus.OK);
        httpResponse.headers().clear();
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        byte[] content;
        if (HttpUtil.checkHead(httpRequest, HttpHeaderNames.HOST, "^.*\\.baidupcs\\.com.*$")) {
          content = "<script>window.close();</script>".getBytes();
        }else{
          content = "<script>window.history.go(-1);</script>".getBytes();
        }
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        clientChannel.writeAndFlush(httpResponse);
        HttpContent httpContent = new DefaultLastHttpContent();
        httpContent.content().writeBytes(content);
        clientChannel.writeAndFlush(httpContent);
        clientChannel.close();
        httpDownDispatch.dispatch(httpDownInfo);
      }
    };
  }

  public interface HttpDownDispatch {

    void dispatch(HttpDownInfo httpDownInfo);
  }
}
