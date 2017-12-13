package lee.study.down.intercept;

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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;

/**
 * 破解百度云PC浏览器版大文件下载限制
 */
public class BdyIntercept extends HttpProxyIntercept {

  private boolean isMatch = false;
  private List<ByteBuf> contents;
  private static final String hookJs = "<script>"
      + "var hook=function(){return 'GYun';};"
      + "if(Object.defineProperty){"
      + "Object.defineProperty(navigator,'platform',{get:hook,configurable:true});"
      + "}"
      + "else if(Object.prototype.__defineGetter__){"
      + "navigator.__defineGetter__('platform',hook);"
      + "}"
      + "</script>";
  private ByteBuf contentBuf;

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (HttpDownUtil.checkUrl(httpRequest, "^pan.baidu.com/disk/home.*$")
        && "text/html".equalsIgnoreCase(httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE))) {
      isMatch = true;
      if (contents == null) {
        contents = new ArrayList<>(); //初始化一次响应内容缓存
      }
      //解压gzip响应
      if ("gzip".equalsIgnoreCase(httpResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING))) {
        pipeline.reset3();
        proxyChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
        proxyChannel.pipeline().fireChannelRead(httpResponse);
      } else {
        httpResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
        contentBuf = PooledByteBufAllocator.DEFAULT.buffer();
        contentBuf.writeBytes(hookJs.getBytes());
      }
      //直接调用默认拦截器，跳过下载拦截器
      pipeline.getDefault().afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
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
          //转化成gzip编码
          byte[] temp = new byte[contentBuf.readableBytes()];
          contentBuf.readBytes(temp);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          GZIPOutputStream outputStream = new GZIPOutputStream(baos);
          outputStream.write(temp);
          outputStream.finish();
          HttpContent hookHttpContent = new DefaultLastHttpContent();
          hookHttpContent.content().writeBytes(baos.toByteArray());
          pipeline.getDefault()
              .afterResponse(clientChannel, proxyChannel, hookHttpContent, pipeline);
        }
      } finally {
        ReferenceCountUtil.release(httpContent);
      }
    } else {
      pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
    }
  }

}
