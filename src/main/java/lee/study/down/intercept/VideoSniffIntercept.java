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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import lee.study.down.intercept.common.ResponseTextIntercept;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;

/**
 * 嗅探网站视频资源
 */
public class VideoSniffIntercept extends ResponseTextIntercept {

  private static String hookJs;

  static {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(
          Thread.currentThread().getContextClassLoader()
              .getResourceAsStream("hookjs/blobSniff.js")));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      sb.insert(0, "<script type=\"text/javascript\">");
      sb.append("</script>");
      hookJs = sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean match(HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    return isHtml(httpResponse, pipeline);
  }

  @Override
  public String hookResponse() {
    return hookJs;
  }
}
