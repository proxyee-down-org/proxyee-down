package lee.study.down.plug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lee.study.down.model.PluginBean;
import lee.study.down.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginContent {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginContent.class);
  private static final Map<String, PluginBean> content = new ConcurrentHashMap<>();

  public static void init() {
    try {
      URL url = Thread.currentThread().getContextClassLoader()
          .getResource("hookjs");
      URLConnection connection = url.openConnection();
      if (connection instanceof JarURLConnection) {
        JarURLConnection jarURLConnection = (JarURLConnection) connection;
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (entry.getName().matches("^.*hookjs/[^/]+$")) {
            String key = entry.getName().substring(entry.getName().indexOf("hookjs/") + 7);
            set(key, PluginUtil.checkAndUpdateLocalPlugin(key, jarFile.getInputStream(entry)));
          }
        }
        jarFile.close();
      } else {
        File file = new File(url.getPath());
        for (File hook : file.listFiles()) {
          set(hook.getName(),
              PluginUtil.checkAndUpdateLocalPlugin(hook.getName(), new FileInputStream(hook)));
        }
      }
    } catch (Exception e) {
      LOGGER.error("plugin content init error", e);
    }
  }

  public static void set(String key, PluginBean value) {
    if (value != null) {
      content.put(key, value);
    }
  }

  public static PluginBean get(String key) {
    return content.get(key);
  }

  public static void main(String[] args) throws IOException, URISyntaxException {
    init();
  }
}
