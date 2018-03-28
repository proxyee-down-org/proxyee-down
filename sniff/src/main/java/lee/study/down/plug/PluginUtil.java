package lee.study.down.plug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lee.study.down.model.PluginBean;
import lee.study.down.util.FileUtil;
import lee.study.down.util.PathUtil;

public class PluginUtil {

  private static final String PLUG_PATH = PathUtil.ROOT_PATH + "plugs";

  public static PluginBean getPluginBean(String key, InputStream inputStream, float currentVersion)
      throws IOException {
    PluginBean pluginBean = new PluginBean();
    FileOutputStream outputStream = null;
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
            String localFile = PLUG_PATH + File.separator + key;
            FileUtil.createFileSmart(localFile);
            outputStream = new FileOutputStream(localFile);
          } else {
            return null;
          }
          num++;
        }
        String temp = line + "\r\n";
        sb.append(temp);
        outputStream.write(temp.getBytes("UTF-8"));
      }
      pluginBean.setContent(sb.toString());
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
    return pluginBean;
  }

  private static PluginBean getPluginBean(InputStream inputStream)
      throws IOException {
    return getPluginBean(null, inputStream, -1F);
  }

  public static PluginBean checkAndUpdateLocalPlugin(String key, InputStream inputStream)
      throws IOException {
    String localFile = PLUG_PATH + File.separator + key;
    float localVersion = -1F;
    if (FileUtil.exists(localFile)) {
      try (
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(new FileInputStream(PLUG_PATH + File.separator + key), "UTF-8"))
      ) {
        String str = reader.readLine();
        localVersion = Float.parseFloat(str.substring(2));
      } catch (Exception e) {

      }
    }
    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, "UTF-8"))
    ) {
      String str = reader.readLine();
      float targetVersion = Float.parseFloat(str.substring(2));
      //plug需要更新
      if (targetVersion > localVersion) {
        FileUtil.createFileSmart(localFile);
        try (
            FileOutputStream outputStream = new FileOutputStream(localFile);
        ) {
          outputStream.write((str + "\r\n").getBytes("UTF-8"));
          String line;
          while ((line = reader.readLine()) != null) {
            outputStream.write((line + "\r\n").getBytes("UTF-8"));
          }
        }
      }
    }
    return getPluginBean(new FileInputStream(localFile));
  }
}
