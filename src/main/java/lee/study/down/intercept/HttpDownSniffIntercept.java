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
      HttpHeaders httpHeaders = httpResponse.headers();
      String disposition = httpHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
      if (disposition != null) {  //先根据CONTENT_DISPOSITION:ATTACHMENT来判断是否下载请求
        //没有Range请求头(audio标签发起的)并且不是ajax请求(没有X-Requested-With请求头)
        //检测是请求头是否有自定义X-，正常触发超链接下载是不会出现X-打头的扩展请求头
        if (disposition.contains(HttpHeaderValues.ATTACHMENT) && !httpRequest.headers()
            .contains(HttpHeaderNames.RANGE) && !HttpDownUtil
            .checkHeadKey(httpRequest.headers(), "^(?i)X-.*$")) {
          downFlag = true;
        }
      }
      if (!downFlag) {  //再根据URL和CONTENT_TYPE来判断是否下载请求
        if (httpRequest.uri().matches("^.*\\.[^./]{1,5}$")) { //url后缀为.xxx
          String contentType = httpHeaders.get(HttpHeaderNames.CONTENT_TYPE);
          if (contentType != null
              && contentType.contains("application/")
              && !contentType.contains("javascript")
              && !contentType.contains("x-navimap")
              && !contentType.contains("font-")
              && !contentType.contains("json")
              && !contentType.contains("shockwave-flash")) {
            //字体文件情况排除 referer为.css后缀 uri为.woff或.ttf
            try {
              String referer = httpRequest.headers().get(HttpHeaderNames.REFERER);
              if (referer != null && contentType.contains("application/octet-stream") &&
                  (referer.matches("^.*\\.(?i)css[^.]*$") || httpRequest.uri()
                      .matches("^.*\\.(?i)(?:wof|tt)f[^.]*$"))) {
                downFlag = false;
              } else {
                downFlag = true;
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

          }
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
