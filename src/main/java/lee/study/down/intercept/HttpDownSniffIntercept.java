package lee.study.down.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.down.util.HttpDownUtil;

public class HttpDownSniffIntercept extends HttpProxyIntercept {

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel,
      final HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
    boolean downFlag = false;
    if ((httpResponse.status().code() + "").indexOf("20") == 0) { //响应码为20x
      HttpHeaders httpResHeaders = httpResponse.headers();
      String accept = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
      if (accept != null
          && accept.matches("^.*text/html.*$")  //直接url的方式访问不是以HTML标签加载的(a标签除外)
          && !httpResHeaders.get(HttpHeaderNames.CONTENT_TYPE)
          .matches("^.*text/.*$")) { //响应体不是text/html报文
        //有两种情况进行下载 1.url后缀为.xxx  2.带有CONTENT_DISPOSITION:ATTACHMENT响应头
        String disposition = httpResHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
        if (httpRequest.uri().matches("^.*\\.[^./]{1,5}(\\?[^?]*)?$")
            || (disposition != null && disposition.contains(HttpHeaderValues.ATTACHMENT))) {
          downFlag = true;
        }
      }
      if (downFlag) {   //如果是下载
        proxyChannel.close();//关闭嗅探下载连接
        System.out.println("=====================下载===========================");
        System.out.println(httpRequest.toString());
        System.out.println("------------------------------------------------");
        System.out.println(httpResponse.toString());
        System.out.println("================================================");
        pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
      }
    }
    pipeline.getDefault().afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
  }
}
