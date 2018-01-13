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
import lee.study.down.model.HttpRequestInfo;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownSniffIntercept extends HttpProxyIntercept {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownSniffIntercept.class);

  private ByteBuf content;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    pipeline.setHttpRequest(HttpRequestInfo.adapter(httpRequest));
    String contentLength = httpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH);
    //缓存request content
    if (contentLength != null) {
      content = PooledByteBufAllocator.DEFAULT.buffer();
    }
    pipeline.beforeRequest(clientChannel, httpRequest);
  }

  @Override
  public void beforeRequest(Channel clientChannel, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
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
    boolean downFlag = false;
    if ((httpResponse.status().code() + "").indexOf("20") == 0) { //响应码为20x
      HttpHeaders httpResHeaders = httpResponse.headers();
      String accept = pipeline.getHttpRequest().headers().get(HttpHeaderNames.ACCEPT);
      String contentType = httpResHeaders.get(HttpHeaderNames.CONTENT_TYPE);
      if (accept != null
          && accept.matches("^.*text/html.*$")  //直接url的方式访问不是以HTML标签加载的(a标签除外)
          && contentType != null
          && !contentType.matches("^.*text/.*$")) { //响应体不是text/html报文
        //有两种情况进行下载 1.url后缀为.xxx  2.带有CONTENT_DISPOSITION:ATTACHMENT响应头
        String disposition = httpResHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
        if (pipeline.getHttpRequest().uri().matches("^.*\\.[^./]{1,5}(\\?[^?]*)?$")
            || (disposition != null && disposition.contains(HttpHeaderValues.ATTACHMENT))) {
          downFlag = true;
        }
      }
      HttpRequestInfo httpRequestInfo = (HttpRequestInfo) pipeline.getHttpRequest();
      if (downFlag) {   //如果是下载
        proxyChannel.close();//关闭嗅探下载连接
        LOGGER.debug("=====================下载===========================\n" +
            pipeline.getHttpRequest().toString() + "\n" +
            httpResponse.toString() + "\n" +
            "================================================");
        //原始的请求协议
        httpRequestInfo.setRequestProto(pipeline.getRequestProto());
        pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
      } else {
        if (httpRequestInfo.content() != null) {
          httpRequestInfo.setContent(null);
        }
      }
    }
    pipeline.getDefault().afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
  }
}
