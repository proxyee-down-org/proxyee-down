package org.pdown.gui.extension.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.pdown.core.util.FileUtil;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.Meta;
import org.pdown.gui.util.AppUtil;

public class ExtensionUtil {

  /**
   * 安装扩展
   */
  public static void install(String server, String path, String files) throws Exception {
    download(server, path, path, files);
  }

  /**
   * 更新扩展,先把扩展文件下载到临时目录中
   */
  public static void update(String server, String path, String files) throws Exception {
    String extDir = ExtensionContent.EXT_DIR + File.separator + path;
    String tmpPath = path + "_tmp";
    String extTmpPath = ExtensionContent.EXT_DIR + File.separator + tmpPath;
    String extBakPath = ExtensionContent.EXT_DIR + File.separator + path + "_bak";
    try {
      download(server, path, tmpPath, files);
      //备份老版本扩展
      copy(new File(extDir), new File(extBakPath));
      //备份扩展配置
      String configPath = extDir + File.separator + Meta.CONFIG_FILE;
      if (FileUtil.exists(configPath)) {
        Files.copy(Paths.get(configPath), Paths.get(extTmpPath + File.separator + Meta.CONFIG_FILE), StandardCopyOption.REPLACE_EXISTING);
      }
      String configBakPath = extDir + File.separator + Meta.CONFIG_FILE + ".bak";
      if (FileUtil.exists(configBakPath)) {
        Files.copy(Paths.get(configBakPath), Paths.get(extTmpPath + File.separator + Meta.CONFIG_FILE + ".bak"), StandardCopyOption.REPLACE_EXISTING);
      }
      try {
        //删除原始扩展目录并将临时目录重命名
        FileUtil.deleteIfExists(extDir);
      } catch (Exception e) {
        //删除失败还原扩展
        copy(new File(extBakPath), new File(extDir));
        throw new IOException(e);
      } finally {
        FileUtil.deleteIfExists(extBakPath);
      }
      new File(extTmpPath).renameTo(new File(extDir));
    } finally {
      //删除临时目录
      FileUtil.deleteIfExists(extTmpPath);
    }
  }

  /**
   * 根据扩展的路径和文件列表，下载对应的文件
   */
  private static void download(String server, String path, String writePath, String files) throws Exception {
    String extDir = ExtensionContent.EXT_DIR + File.separator + writePath;
    if (!FileUtil.exists(extDir)) {
      Files.createDirectories(Paths.get(extDir));
    }
    for (String fileName : files.split(",")) {
      AppUtil.download(server + path + fileName, extDir + File.separator + fileName);
    }
  }

  public static void copy(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      copyDirectory(sourceLocation, targetLocation);
    } else {
      copyFile(sourceLocation, targetLocation);
    }
  }

  private static void copyDirectory(File source, File target) throws IOException {
    if (!target.exists()) {
      target.mkdir();
    }
    for (String f : source.list()) {
      copy(new File(source, f), new File(target, f));
    }
  }

  private static void copyFile(File source, File target) throws IOException {
    if (target.exists()) {
      return;
    }
    try (
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target)
    ) {
      byte[] buf = new byte[1024];
      int length;
      while ((length = in.read(buf)) > 0) {
        out.write(buf, 0, length);
      }
    }
  }
}
