package lee.study.down.intercept;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayList;
import java.util.List;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.down.util.HttpDownUtil;

/**
 * 破解百度云PC浏览器版大文件下载限制
 */
public class BdyIntercept extends HttpProxyIntercept {

  private boolean isMatch = false;
  private List<ByteBuf> contents;

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (HttpDownUtil.checkUrl(httpRequest, "^pan.baidu.com/disk/home.*$")
        && "text/html".equalsIgnoreCase(httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE))) {
      isMatch = true;
      if(contents==null){
        contents = new ArrayList<>(); //初始化一次响应内容缓存
      }
      //解压gzip响应
      if ("gzip".equalsIgnoreCase(httpResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING))) {
        pipeline.reset3();
        proxyChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
        proxyChannel.pipeline().fireChannelRead(httpResponse);
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
      contents.add(httpContent.content());
      if (httpContent instanceof LastHttpContent) {
        //移除gizp解压handle
        proxyChannel.pipeline().remove("decompress");
        ByteBuf hookJsBuf = PooledByteBufAllocator.DEFAULT.buffer();
        String hookJs = "<script>"
            + "var hook=function(){return 'GYun';};"
            + "if(Object.defineProperty){"
            + "Object.defineProperty(navigator,'platform',{get:hook,configurable:true});"
            + "}"
            + "else if(Object.prototype.__defineGetter__){"
            + "navigator.__defineGetter__('platform',hook);"
            + "}"
            + "</script>";
        hookJsBuf.writeBytes(hookJs.getBytes());
        contents.add(0,hookJsBuf);
        HttpContent lastHttpContent = new DefaultLastHttpContent();
        for(ByteBuf byteBuf:contents){
          lastHttpContent.content().writeBytes(byteBuf);
        }
        pipeline.getDefault().afterResponse(clientChannel, proxyChannel, lastHttpContent, pipeline);
        for(ByteBuf byteBuf:contents){
          ReferenceCountUtil.release(byteBuf);
        }
      }
    } else {
      pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
    }
  }

  public static void main(String[] args) {
    ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
//    byteBuf.alloc().calculateNewCapacity(0,1000);
    System.out.println(byteBuf.maxCapacity());
    System.out.println(byteBuf.capacity());
  }
}
