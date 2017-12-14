package lee.study.down.intercept;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.model.HttpRequestInfo;

public class HttpDownSniffIntercept extends HttpProxyIntercept {

  private ByteBuf content;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    String contentLength = httpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH);
    //缓存request content
    if (contentLength != null) {
      content = PooledByteBufAllocator.DEFAULT.buffer();
    }
    pipeline.beforeRequest(clientChannel, httpRequest);
  }

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (content != null) {
      ByteBuf temp = httpContent.content().slice();
      content.writeBytes(temp);
      if (httpContent instanceof LastHttpContent) {
        try {
          byte[] contentBts = new byte[content.readableBytes()];
          content.readBytes(contentBts);
          ((HttpRequestInfo) httpRequest).setContent(contentBts);
        } finally {
          ReferenceCountUtil.release(content);
          content = null; //状态回归
        }
      }
    }
    pipeline.beforeRequest(clientChannel, httpRequest, httpContent);
  }

  public static void main(String[] args) {
    ByteBuf content = PooledByteBufAllocator.DEFAULT.heapBuffer();
    System.out.println(content.refCnt());
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpRequest httpRequest,
      HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
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
        pipeline.afterResponse(clientChannel, proxyChannel, httpRequest, httpResponse);
      } else {
        HttpRequestInfo httpRequestInfo = (HttpRequestInfo) httpRequest;
        if (httpRequestInfo.content() != null) {
          httpRequestInfo.setContent(null);
        }
      }
    }
    pipeline.getDefault()
        .afterResponse(clientChannel, proxyChannel, httpRequest, httpResponse, pipeline);
  }
}
