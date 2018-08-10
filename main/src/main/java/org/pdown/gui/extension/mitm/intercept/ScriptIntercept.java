package org.pdown.gui.extension.mitm.intercept;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.util.ByteUtil;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.ExtensionInfo.ContentScript;

public class ScriptIntercept extends FullResponseIntercept {

  private Map<String, List<ContentScript>> matchScriptMap = new HashMap<>();

  @Override
  public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    if (isHtml(httpRequest, httpResponse)) {
      doMatchScripts(httpRequest);
      return matchScriptMap.size() > 0;
    }
    return false;
  }

  //匹配url对应的js脚本
  private void doMatchScripts(HttpRequest httpRequest) {
    List<ExtensionInfo> extensionInfoList = ExtensionContent.get();
    if (extensionInfoList != null && extensionInfoList.size() > 0) {
      for (ExtensionInfo extensionInfo : extensionInfoList) {
        if (extensionInfo.getContentScripts() != null && extensionInfo.getContentScripts().size() > 0) {
          for (ContentScript contentScript : extensionInfo.getContentScripts()) {
            if (contentScript.getMatches() != null && contentScript.getMatches().length > 0) {
              for (String match : contentScript.getMatches()) {
                if (HttpUtil.checkUrl(httpRequest, match)) {
                  String key = extensionInfo.getPath();
                  List<ContentScript> contentScriptList = matchScriptMap.get(key);
                  if (contentScriptList == null) {
                    contentScriptList = new ArrayList<>();
                    matchScriptMap.put(key, contentScriptList);
                  }
                  contentScriptList.add(contentScript);
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
    StringBuilder scriptsBuilder = new StringBuilder();
    for (Entry<String, List<ContentScript>> entry : matchScriptMap.entrySet()) {
      for (ContentScript contentScript : entry.getValue()) {
        if (contentScript.getScripts() != null && contentScript.getScripts().length > 0) {
          for (String script : contentScript.getScripts()) {
            File scriptFile = new File(entry.getKey() + File.separator + script);
            if (scriptFile.exists() && scriptFile.isFile()) {
              scriptsBuilder.append("<script type=\"text/javascript\">");
              try {
                scriptsBuilder.append(new String(Files.readAllBytes(scriptFile.toPath()), "UTF-8"));
              } catch (IOException e) {
              }
              scriptsBuilder.append("</script>");
            }
          }
        }
      }
    }
    if (scriptsBuilder.length() > 0) {
      int index = ByteUtil.findText(httpResponse.content(), "<head>");
      ByteUtil.insertText(httpResponse.content(), index == -1 ? 0 : index, scriptsBuilder.toString(), Charset.forName("UTF-8"));
    }
  }
}
