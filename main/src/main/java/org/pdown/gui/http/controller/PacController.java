package org.pdown.gui.http.controller;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.util.Set;
import org.pdown.gui.DownApplication;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("pac")
public class PacController {

  private static final String PAC_TEMPLATE = "function FindProxyForURL(url, host) {"
      + "  if (isInNet(host, '127.0.0.1', '255.0.0.255')"
      + "      || isInNet(dnsResolve(host), '127.0.0.1', '255.0.0.255')) {"
      + "    return 'DIRECT';"
      + "  }"
      + "  var domains = [{domains}];"
      + "  var match = false;"
      + "  for (var i = 0; i < domains.length; i++) {"
      + "    if (shExpMatch(host, domains[i])) {"
      + "      match = true;"
      + "      break;"
      + "    }"
      + "  }"
      + "  return match ? 'PROXY 127.0.0.1:{port}' : 'DIRECT';"
      + "}";

  @RequestMapping("pdown.pac")
  public FullHttpResponse build(Channel channel, FullHttpRequest request) throws Exception {
    Set<String> domains = ExtensionContent.getProxyWildCards();
    String pacContent = PAC_TEMPLATE.replace("{port}", DownApplication.INSTANCE.PROXY_PORT + "");
    if (domains != null && domains.size() > 0) {
      StringBuilder domainsBuilder = new StringBuilder();
      for (String domain : domains) {
        if (domainsBuilder.length() != 0) {
          domainsBuilder.append(",");
        }
        domainsBuilder.append("'" + domain + "'");
      }
      pacContent = pacContent.replace("{domains}", domainsBuilder.toString());
    } else {
      pacContent = pacContent.replace("{domains}", "");
    }
    FullHttpResponse httpResponse = HttpHandlerUtil.buildContent(pacContent, "application/x-ns-proxy-autoconfig");
    httpResponse.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
    httpResponse.headers().set(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE);
    httpResponse.headers().set(HttpHeaderNames.EXPIRES, 0);
    return httpResponse;
  }

}
