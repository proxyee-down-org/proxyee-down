package org.pdown.gui.extension.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.pdown.core.util.FileUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.Meta;
import org.pdown.gui.extension.jsruntime.JavascriptEngine;
import org.pdown.gui.util.AppUtil;
import org.pdown.gui.util.ConfigUtil;

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

  public static String readRuntimeTemplate(ExtensionInfo extensionInfo) {
    String template = "";
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("extension/runtime.js")))
    ) {
      template = reader.lines().collect(Collectors.joining("\n"));
      template = template.replace("${version}", ConfigUtil.getString("version"));
      template = template.replace("${apiPort}", DownApplication.INSTANCE.API_PORT + "");
      template = template.replace("${frontPort}", DownApplication.INSTANCE.FRONT_PORT + "");
      template = template.replace("${uiMode}", PDownConfigContent.getInstance().get().getUiMode() + "");
      String settingJson = "{}";
      if (extensionInfo.getMeta().getSettings() != null) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          settingJson = objectMapper.writeValueAsString(extensionInfo.getMeta().getSettings());
        } catch (JsonProcessingException e) {
        }
      }
      template = template.replace("${settings}", settingJson);
    } catch (IOException e) {
    }
    return template;
  }

  /**
   * 创建扩展环境的js引擎，可以在引擎中访问pdown对象
   */
  public static ScriptEngine buildExtensionRuntimeEngine(ExtensionInfo extensionInfo) throws ScriptException, NoSuchMethodException, FileNotFoundException {
    //初始化js引擎
    ScriptEngine engine = JavascriptEngine.buildEngine();
    //加载运行时脚本
    Object runtime = engine.eval(ExtensionUtil.readRuntimeTemplate(extensionInfo));
    engine.put("pdown", runtime);
    //加载扩展脚本
    engine.eval(new FileReader(Paths.get(extensionInfo.getMeta().getFullPath(), extensionInfo.getHookScript().getScript()).toFile()));
    return engine;
  }
}
