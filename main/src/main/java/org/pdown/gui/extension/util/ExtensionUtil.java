package org.pdown.gui.extension.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.pdown.core.util.FileUtil;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.Meta;

public class ExtensionUtil {

  /**
   * 安装扩展
   */
  public static void install(String server, String path, String files) throws Exception {
    download(server, path, files);
  }

  /**
   * 更新扩展,先把扩展文件下载到临时目录中
   */
  public static void update(String server, String path, String files) throws Exception {
    String extDir = ExtensionContent.EXT_DIR + File.separator + path;
    String tmpPath = path + "_tmp";
    String extTmpPath = ExtensionContent.EXT_DIR + File.separator + tmpPath;
    try {
      download(server, tmpPath, files);
      //备份插件配置
      String configPath = extDir + File.separator + Meta.CONFIG_FILE;
      if (FileUtil.exists(configPath)) {
        Files.copy(Paths.get(configPath), Paths.get(extTmpPath + File.separator + Meta.CONFIG_FILE), StandardCopyOption.REPLACE_EXISTING);
      }
      String configBakPath = extDir + File.separator + Meta.CONFIG_FILE + ".bak";
      if (FileUtil.exists(configBakPath)) {
        Files.copy(Paths.get(configBakPath), Paths.get(extTmpPath + File.separator + Meta.CONFIG_FILE + ".bak"), StandardCopyOption.REPLACE_EXISTING);
      }
      //删除原始扩展目录并将临时目录重命名
      FileUtil.deleteIfExists(extDir);
      new File(extTmpPath).renameTo(new File(extDir));
    } finally {
      //删除临时目录
      FileUtil.deleteIfExists(extTmpPath);
    }
  }

  /**
   * 根据扩展的路径和文件列表，下载对应的文件
   */
  private static void download(String server, String path, String files) throws Exception {
    String extDir = ExtensionContent.EXT_DIR + File.separator + path;
    FileUtil.createDir(extDir);
    for (String fileName : files.split(",")) {
      URL url = new URL(server + path + fileName);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(30000);
      connection.setReadTimeout(60000);
      File file = new File(extDir + File.separator + fileName);
      if (!file.exists() || file.isDirectory()) {
        FileUtil.createFileSmart(file.getPath());
      }
      try (
          InputStream input = connection.getInputStream();
          FileOutputStream output = new FileOutputStream(file)
      ) {
        byte[] bts = new byte[8192];
        int len;
        while ((len = input.read(bts)) != -1) {
          output.write(bts, 0, len);
        }
      }
    }
  }
}
