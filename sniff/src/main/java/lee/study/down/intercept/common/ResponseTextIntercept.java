package lee.study.down.intercept.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;
import lee.study.down.util.ByteUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;

public abstract class ResponseTextIntercept extends HttpProxyIntercept {

  private boolean isMatch = false;
  private boolean isGzip = false;
  private ByteBuf contentBuf;

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (match(httpResponse, pipeline)) {
      isMatch = true;
      //解压gzip响应
      if ("gzip".equalsIgnoreCase(httpResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING))) {
        isGzip = true;
        pipeline.reset3();
        proxyChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
        proxyChannel.pipeline().fireChannelRead(httpResponse);
      } else {
        if (isGzip) {
          httpResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
        }
        contentBuf = PooledByteBufAllocator.DEFAULT.buffer();
      }
      httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
      httpResponse.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
    } else {
      isMatch = false;
      pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
    }
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (isMatch) {
      try {
        contentBuf.writeBytes(httpContent.content());
        if (httpContent instanceof LastHttpContent) {
          ByteUtil.insertText(contentBuf, ByteUtil.findText(contentBuf, "<head>"), hookResponse(), Charset.forName("UTF-8"));
          HttpContent hookHttpContent = new DefaultLastHttpContent();
          long contentLength;
          if (isGzip) { //转化成gzip编码
            byte[] temp = new byte[contentBuf.readableBytes()];
            contentBuf.readBytes(temp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream outputStream = new GZIPOutputStream(baos);
            outputStream.write(temp);
            outputStream.finish();
            byte[] bts = baos.toByteArray();
            contentLength = bts.length;
            hookHttpContent.content().writeBytes(bts);
          } else {
            contentLength = contentBuf.readableBytes();
            hookHttpContent.content().writeBytes(contentBuf);
          }
          ReferenceCountUtil.release(contentBuf);
          //修改响应头Content-length
          pipeline.getHttpResponse().headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
          pipeline.getDefault().afterResponse(clientChannel, proxyChannel, pipeline.getHttpResponse(), pipeline);
          //写出hook后的响应体
          pipeline.getDefault().afterResponse(clientChannel, proxyChannel, hookHttpContent, pipeline);
        }
      } finally {
        ReferenceCountUtil.release(httpContent);
      }
    } else {
      pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
    }
  }

  protected boolean isHtml(HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    String accept = pipeline.getHttpRequest().headers().get(HttpHeaderNames.ACCEPT);
    String contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    return httpResponse.status().code() == 200 && accept != null && accept
        .matches("^.*text/html.*$") && contentType != null && contentType
        .matches("^text/html.*$");
  }

  public abstract boolean match(HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline);

  public abstract String hookResponse();
}
