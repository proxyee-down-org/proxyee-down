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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import org.pdown.core.util.FileUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.HookScript;
import org.pdown.gui.extension.HookScript.Event;
import org.pdown.gui.extension.Meta;
import org.pdown.gui.extension.jsruntime.JavascriptEngine;
import org.pdown.gui.http.controller.NativeController;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.pdown.gui.util.AppUtil;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.rest.form.TaskForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ExtensionUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionUtil.class);

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
        Path bakConfigPath = Paths.get(extTmpPath + File.separator + Meta.CONFIG_FILE);
        FileUtil.createFileSmart(bakConfigPath.toFile().getAbsolutePath());
        Files.copy(Paths.get(configPath), bakConfigPath, StandardCopyOption.REPLACE_EXISTING);
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

  /**
   * 运行一个js方法
   */
  public static Object invoke(ExtensionInfo extensionInfo, Event event, Object param, boolean async) throws NoSuchMethodException, ScriptException, FileNotFoundException, InterruptedException {
    //初始化js引擎
    ScriptEngine engine = ExtensionUtil.buildExtensionRuntimeEngine(extensionInfo);
    Invocable invocable = (Invocable) engine;
    //执行resolve方法
    Object result = invocable.invokeFunction(StringUtils.isEmpty(event.getMethod()) ? event.getOn() : event.getMethod(), param);
    //结果为null或者异步调用直接返回
    if (result == null || async) {
      return result;
    }
    final Object[] ret = {null};
    //判断是不是返回Promise对象
    ScriptContext ctx = new SimpleScriptContext();
    ctx.setAttribute("result", result, ScriptContext.ENGINE_SCOPE);
    boolean isPromise = (boolean) engine.eval("!!result&&typeof result=='object'&&typeof result.then=='function'", ctx);
    if (isPromise) {
      //如果是返回的Promise则等待执行完成
      CountDownLatch countDownLatch = new CountDownLatch(1);
      invocable.invokeMethod(result, "then", (Function) o -> {
        try {
          ret[0] = o;
        } catch (Exception e) {
          LOGGER.error("An exception occurred while resolve()", e);
        } finally {
          countDownLatch.countDown();
        }
        return null;
      });
      invocable.invokeMethod(result, "catch", (Function) o -> {
        countDownLatch.countDown();
        return null;
      });
      //等待解析完成
      countDownLatch.await();
    } else {
      ret[0] = result;
    }
    return ret[0];
  }

  public static void main(String[] args) {
    String url = "https://d.pcs.baidu.com/file/ab83d33b3f250a6ff472b8ffa17c3e5f?fid=336129479-250528-831181624029689&dstime=1541581115&rt=sh&sign=FDtAERVY-DCb740ccc5511e5e8fedcff06b081203-aILjqoJW3OEzB4%2Bu7Wnxz63PUho%3D&expires=8h&chkv=1&chkbd=0&chkpc=et&dp-logid=7206180716394015240&dp-callid=0&shareid=3786359813&r=663179228";
    String[] urlArray = url.split("\\?");
    StringBuilder params = new StringBuilder(urlArray[1]);
    String path = urlArray[0].substring(urlArray[0].lastIndexOf("/") + 1);
    params.append("&path=" + path)
        .append("&check_blue=1")
        .append("&clienttype=8")
        .append("&devuid=BDIMXV2-O_9A2DB23216984690875184DCA864434E-C_0-D_Z4Y7YWL9-M_408D5C4224FB-V_D8F90423")
        .append("&dtype=1")
        .append("&eck=1")
        .append("&ehps=1")
        .append("&err_ver=1")
        .append("&es=1")
        .append("&esl=1")
        .append("&method=locatedownload")
        .append("&ver=4")
        .append("&version=2.1.13.11")
        .append("&version_app=6.4.0.6");
    System.out.println(params.toString());
  }
}
