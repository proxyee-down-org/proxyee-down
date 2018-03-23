package lee.study.down.plug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lee.study.down.model.PluginBean;

public class PluginUtil {

  public static PluginBean getPluginBean(InputStream inputStream, float currentVersion)
      throws IOException {
    PluginBean pluginBean = new PluginBean();
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
    ) {
      StringBuilder sb = new StringBuilder();
      String line;
      int num = 1;
      while ((line = reader.readLine()) != null) {
        if (num == 1) {
          //版本号
          float newVersion = Float.parseFloat(line.substring(2));
          if (newVersion > currentVersion) {
            pluginBean.setVersion(newVersion);
          } else {
            return null;
          }
          num++;
        }
        sb.append(line + "\r\n");
      }
      pluginBean.setContent(sb.toString());
    }
    return pluginBean;
  }

  public static PluginBean getPluginBean(InputStream inputStream)
      throws IOException {
    return getPluginBean(inputStream, -1F);
  }
}
