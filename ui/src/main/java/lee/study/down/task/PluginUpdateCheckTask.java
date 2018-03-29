package lee.study.down.task;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import lee.study.down.model.PluginBean;
import lee.study.down.plug.PluginContent;
import lee.study.down.plug.PluginUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查脚本
 */
public class PluginUpdateCheckTask extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginUpdateCheckTask.class);
  private static final String HOST = "https://github.com/monkeyWie/proxyee-down-plugin/raw/master/";

  @Override
  public void run() {
    try {
      Document document = Jsoup.connect("https://github.com/monkeyWie/proxyee-down-plugin").get();
      for (String name : document.select("td.content span.css-truncate.css-truncate-target")
          .eachText()) {
        URL url = new URL(HOST + name);
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        PluginBean pluginBean = PluginUtil
            .checkAndUpdateLocalPlugin(name, connection.getInputStream());
        PluginBean current = PluginContent.get(name);
        if (current != null && pluginBean.getVersion() > current.getVersion()) {
          PluginContent.set(name, pluginBean);
        }
      }
    } catch (IOException e) {
      LOGGER.error("plugin set error", e);
    }
  }
}
