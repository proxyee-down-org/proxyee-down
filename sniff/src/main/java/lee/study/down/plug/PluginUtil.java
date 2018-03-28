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

  public static final String PLUG_PATH = PathUtil.ROOT_PATH + "plugs";

  public static PluginBean getPluginBean(InputStream inputStream)
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
          pluginBean.setVersion(newVersion);
          num++;
        }
        String temp = line + "\r\n";
        sb.append(temp);
      }
      pluginBean.setContent(sb.toString());
    }
    return pluginBean;
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
        if (!FileUtil.exists(PLUG_PATH)) {
          FileUtil.createDirSmart(PLUG_PATH);
        }
        if (FileUtil.exists(localFile)) {
          new File(localFile).delete();
        }
        new File(localFile).createNewFile();
        try (
            FileOutputStream outputStream = new FileOutputStream(localFile)
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
