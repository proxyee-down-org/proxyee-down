package org.pdown.gui.extension.mitm.intercept;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.util.ByteUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.gui.extension.ContentScript;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.util.ExtensionUtil;

public class ScriptIntercept extends FullResponseIntercept {

  @Override
  public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    return isHtml(httpRequest, httpResponse);
  }

  private static final String INSERT_TOKEN = "</head>";
  private static final String INIT_TEMPLATE = ";(function (pdown) {\n"
      + "  ${content}\n"
      + "})(${runtime})";

  private String readInsertTemplate(ExtensionInfo extensionInfo) {
    String js = ExtensionUtil.readRuntimeTemplate(extensionInfo);
    js = INIT_TEMPLATE.replace("${runtime}", js);
    js = "<script type=\"text/javascript\">\n" + js + "\n</script>";
    return js;
  }

  @Override
  public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    List<ExtensionInfo> extensionInfoList = ExtensionContent.get();
    if (isEmpty(extensionInfoList)) {
      return;
    }
    for (ExtensionInfo extensionInfo : extensionInfoList) {
      if (isEmpty(extensionInfo.getContentScripts())) {
        continue;
      }
      for (ContentScript contentScript : extensionInfo.getContentScripts()) {
        //扩展注入正则表达式与当前访问的url匹配则注入脚本
        String url = HttpDownUtil.getUrl(httpRequest);
        if (contentScript.isMatch(url)) {
          String apiTemplate = readInsertTemplate(extensionInfo);
          StringBuilder scriptsBuilder = new StringBuilder();
          for (String script : contentScript.getScripts()) {
            File scriptFile = new File(extensionInfo.getMeta().getFullPath() + File.separator + script);
            if (scriptFile.exists() && scriptFile.isFile()) {
              try {
                scriptsBuilder.append(new String(Files.readAllBytes(scriptFile.toPath()), "UTF-8"));
              } catch (IOException e) {
              }
            }
          }
          apiTemplate = apiTemplate.replace("${content}", scriptsBuilder.toString());
          int index = ByteUtil.findText(httpResponse.content(), INSERT_TOKEN);
          ByteUtil.insertText(httpResponse.content(), index == -1 ? 0 : index - INSERT_TOKEN.length(), apiTemplate, Charset.forName("UTF-8"));
        }
      }
    }
  }

  private boolean isEmpty(List list) {
    return list == null || list.size() == 0;
  }
}
