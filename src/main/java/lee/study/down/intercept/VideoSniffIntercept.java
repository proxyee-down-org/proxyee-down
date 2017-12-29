package lee.study.down.intercept;

import io.netty.handler.codec.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import lee.study.down.intercept.common.ResponseTextIntercept;
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
