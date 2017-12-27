package lee.study.down.intercept;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
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
import lee.study.down.intercept.common.ResponseTextIntercept;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;

/**
 * 破解百度云PC浏览器版大文件下载限制
 */
public class BdyIntercept extends ResponseTextIntercept {

  private static final String hookJs = "<script type=\"text/javascript\">"
      + "var hook=function(){return 'GYun';};"
      + "if(Object.defineProperty){"
      + "Object.defineProperty(navigator,'platform',{get:hook,configurable:true});"
      + "}"
      + "else if(Object.prototype.__defineGetter__){"
      + "navigator.__defineGetter__('platform',hook);"
      + "}"
      + "</script>";

  @Override
  public boolean match(HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    return HttpDownUtil.checkUrl(pipeline.getHttpRequest(), "^(pan|yun).baidu.com.*$")
        && isHtml(httpResponse, pipeline);
  }

  @Override
  public String hookResponse() {
    return hookJs;
  }

}
