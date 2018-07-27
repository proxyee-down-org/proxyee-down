package lee.study.down.intercept;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import io.netty.handler.codec.http.HttpResponse;
import lee.study.down.intercept.common.ResponseTextIntercept;
import lee.study.down.plug.PluginContent;
import lee.study.down.util.HttpUtil;

/**
 * 破解百度云PC浏览器版大文件下载限制
 */
public class BdyIntercept extends ResponseTextIntercept {

  @Override
  public boolean match(HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    return HttpUtil.checkUrl(pipeline.getHttpRequest(), "^(pan|yun).baidu.com/(disk/home|s/|share/link).*$")
        && isHtml(httpResponse, pipeline);
  }

  @Override
  public String hookResponse() {
    return "<script type=\"text/javascript\">"
        + PluginContent.get("bdyHook.js").getContent()
        + "</script>";
  }

}
