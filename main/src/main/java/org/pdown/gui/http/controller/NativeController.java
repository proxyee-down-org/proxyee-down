package org.pdown.gui.http.controller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import javafx.application.Platform;
import jdk.nashorn.internal.runtime.Undefined;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.com.Components;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.entity.PDownConfigInfo;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.HookScript;
import org.pdown.gui.extension.HookScript.Event;
import org.pdown.gui.extension.mitm.server.PDownProxyServer;
import org.pdown.gui.extension.mitm.util.ExtensionCertUtil;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.extension.util.ExtensionUtil;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.pdown.gui.util.AppUtil;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.gui.util.ExecUtil;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("native")
public class NativeController {

  private static final Logger LOGGER = LoggerFactory.getLogger(NativeController.class);

  @RequestMapping("dirChooser")
  public FullHttpResponse dirChooser(Channel channel, FullHttpRequest request) throws Exception {
    Platform.runLater(() -> {
      File file = Components.dirChooser();
      Map<String, Object> data = null;
      if (file != null) {
        data = new HashMap<>();
        data.put("path", file.getPath());
        data.put("canWrite", file.canWrite());
        data.put("freeSpace", file.getFreeSpace());
        data.put("totalSpace", file.getTotalSpace());
      }
      HttpHandlerUtil.writeJson(channel, data);
    });
    return null;
  }

  @RequestMapping("fileChooser")
  public FullHttpResponse handle(Channel channel, FullHttpRequest request) throws Exception {
    Platform.runLater(() -> {
      File file = Components.fileChooser();
      Map<String, Object> data = null;
      if (file != null) {
        data = new HashMap<>();
        data.put("name", file.getName());
        data.put("path", file.getPath());
        data.put("parent", file.getParent());
        data.put("size", file.length());
      }
      HttpHandlerUtil.writeJson(channel, data);
    });
    return null;
  }

  //启动的时候检查一次
  private boolean checkFlag = true;
  private static final long WEEK = 7 * 24 * 60 * 60 * 1000L;

  @RequestMapping("getInitConfig")
  public FullHttpResponse getInitConfig(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    PDownConfigInfo configInfo = PDownConfigContent.getInstance().get();
    //语言
    data.put("locale", configInfo.getLocale());
    //后台管理API请求地址
    data.put("adminServer", ConfigUtil.getString("adminServer"));
    //是否要检查更新
    boolean needCheckUpdate = false;
    if (checkFlag) {
      int rate = configInfo.getUpdateCheckRate();
      if (rate == 2
          || (rate == 1 && (System.currentTimeMillis() - configInfo.getLastUpdateCheck()) > WEEK)) {
        needCheckUpdate = true;
        checkFlag = false;
        configInfo.setLastUpdateCheck(System.currentTimeMillis());
        PDownConfigContent.getInstance().save();
      }
    }
    data.put("needCheckUpdate", needCheckUpdate);
    //扩展下载服务器列表
    data.put("extFileServers", configInfo.getExtFileServers());
    //软件版本
    data.put("version", ConfigUtil.getString("version"));
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("getConfig")
  public FullHttpResponse getConfig(Channel channel, FullHttpRequest request) throws Exception {
    return HttpHandlerUtil.buildJson(PDownConfigContent.getInstance().get());
  }

  @RequestMapping("setConfig")
  public FullHttpResponse setConfig(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    PDownConfigInfo configInfo = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), PDownConfigInfo.class);
    PDownConfigInfo beforeConfigInfo = PDownConfigContent.getInstance().get();
    boolean proxyChange = (beforeConfigInfo.getProxyConfig() != null && configInfo.getProxyConfig() == null) ||
        (configInfo.getProxyConfig() != null && beforeConfigInfo.getProxyConfig() == null) ||
        (beforeConfigInfo.getProxyConfig() != null && !beforeConfigInfo.getProxyConfig().equals(configInfo.getProxyConfig())) ||
        (configInfo.getProxyConfig() != null && !configInfo.getProxyConfig().equals(beforeConfigInfo.getProxyConfig()));
    boolean localeChange = !configInfo.getLocale().equals(beforeConfigInfo.getLocale());
    BeanUtils.copyProperties(configInfo, beforeConfigInfo);
    if (localeChange) {
      DownApplication.INSTANCE.loadPopupMenu();
      DownApplication.INSTANCE.refreshBrowserMenu();
    }
    //检查到前置代理有变动重启MITM代理服务器
    if (proxyChange && PDownProxyServer.isStart) {
      new Thread(() -> {
        PDownProxyServer.close();
        PDownProxyServer.start(DownApplication.INSTANCE.PROXY_PORT);
      }).start();
    }
    PDownConfigContent.getInstance().save();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("showFile")
  public FullHttpResponse showFile(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String path = (String) map.get("path");
    if (!StringUtils.isEmpty(path)) {
      File file = new File(path);
      if (!file.exists() || OsUtil.isUnix()) {
        Desktop.getDesktop().open(file.getParentFile());
      } else if (OsUtil.isWindows()) {
        ExecUtil.execBlock("explorer.exe", "/select,", file.getPath());
      } else if (OsUtil.isMac()) {
        ExecUtil.execBlock("open", "-R", file.getPath());
      }
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("openUrl")
  public FullHttpResponse openUrl(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String url = (String) map.get("url");
    Desktop.getDesktop().browse(URI.create(URLDecoder.decode(url, "UTF-8")));
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  private static volatile HttpDownBootstrap updateBootstrap;

  @RequestMapping("doUpdate")
  public FullHttpResponse doUpdate(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String url = (String) map.get("path");
    String path = PathUtil.ROOT_PATH + File.separator + "proxyee-down-main.jar.tmp";
    try {
      File updateTmpJar = new File(path);
      if (updateTmpJar.exists()) {
        updateTmpJar.delete();
      }
      updateBootstrap = AppUtil.fastDownload(url, updateTmpJar, new HttpDownCallback() {
        @Override
        public void onDone(HttpDownBootstrap httpDownBootstrap) {
          File updateBakJar = new File(updateTmpJar.getParent() + File.separator + "proxyee-down-main.jar.bak");
          updateTmpJar.renameTo(updateBakJar);
        }

        @Override
        public void onError(HttpDownBootstrap httpDownBootstrap) {
          File file = new File(path);
          if (file.exists()) {
            file.delete();
          }
          httpDownBootstrap.close();
        }
      });
    } catch (Exception e) {
      throw e;
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("getUpdateProgress")
  public FullHttpResponse getUpdateProgress(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    if (updateBootstrap != null) {
      data.put("status", updateBootstrap.getTaskInfo().getStatus());
      data.put("totalSize", updateBootstrap.getResponse().getTotalSize());
      data.put("downSize", updateBootstrap.getTaskInfo().getDownSize());
      data.put("speed", updateBootstrap.getTaskInfo().getSpeed());
    } else {
      data.put("status", 0);
    }
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("doRestart")
  public FullHttpResponse doRestart(Channel channel, FullHttpRequest request) throws Exception {
    System.out.println("proxyee-down-exit");
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  /**
   * 获取已安装的插件列表
   */
  @RequestMapping("getExtensions")
  public FullHttpResponse getExtensions(Channel channel, FullHttpRequest request) throws Exception {
    //刷新扩展信息
    ExtensionContent.load();
    return HttpHandlerUtil.buildJson(ExtensionContent.get());
  }

  /**
   * 安装扩展
   */
  @RequestMapping("installExtension")
  public FullHttpResponse installExtension(Channel channel, FullHttpRequest request) throws Exception {
    return extensionCommon(request, false);
  }

  /**
   * 更新扩展
   */
  @RequestMapping("updateExtension")
  public FullHttpResponse updateExtension(Channel channel, FullHttpRequest request) throws Exception {
    return extensionCommon(request, true);
  }

  /**
   * 加载本地扩展
   */
  @RequestMapping("installLocalExtension")
  public FullHttpResponse installLocalExtension(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    Map<String, Object> map = getJSONParams(request);
    String path = (String) map.get("path");
    //刷新扩展content
    ExtensionInfo loadExt = ExtensionContent.refresh(path, true);
    if (loadExt == null) {
      return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
    }
    data.put("data", loadExt);
    //刷新系统pac代理
    AppUtil.refreshPAC();
    return HttpHandlerUtil.buildJson(data);
  }

  /**
   * 卸载扩展
   */
  @RequestMapping("uninstallExtension")
  public FullHttpResponse uninstallExtension(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    Map<String, Object> map = getJSONParams(request);
    String path = (String) map.get("path");
    boolean local = map.get("local") != null ? (boolean) map.get("local") : false;
    //卸载扩展
    ExtensionContent.remove(path, local);
    //刷新系统pac代理
    AppUtil.refreshPAC();
    return HttpHandlerUtil.buildJson(data);
  }

  private FullHttpResponse extensionCommon(FullHttpRequest request, boolean isUpdate) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String server = (String) map.get("server");
    String path = (String) map.get("path");
    String files = (String) map.get("files");
    if (isUpdate) {
      ExtensionUtil.update(server, path, files);
    } else {
      ExtensionUtil.install(server, path, files);
    }
    //刷新扩展content
    ExtensionContent.refresh(path);
    //刷新系统pac代理
    AppUtil.refreshPAC();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  /**
   * 启用或禁用插件
   */
  @RequestMapping("toggleExtension")
  public FullHttpResponse toggleExtension(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String path = (String) map.get("path");
    boolean enabled = (boolean) map.get("enabled");
    boolean local = map.get("local") != null ? (boolean) map.get("local") : false;
    ExtensionInfo extensionInfo = ExtensionContent.get()
        .stream()
        .filter(e -> e.getMeta().getPath().equals(path))
        .findFirst()
        .get();
    extensionInfo.getMeta().setEnabled(enabled).save();
    //刷新pac
    ExtensionContent.refresh(extensionInfo.getMeta().getFullPath(), local);
    AppUtil.refreshPAC();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("getProxyMode")
  public FullHttpResponse getProxyMode(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("mode", PDownConfigContent.getInstance().get().getProxyMode());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("changeProxyMode")
  public FullHttpResponse changeProxyMode(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    int mode = (int) map.get("mode");
    PDownConfigContent.getInstance().get().setProxyMode(mode);
    //修改系统代理
    if (mode == 1) {
      AppUtil.refreshPAC();
    } else {
      ExtensionProxyUtil.disabledProxy();
    }
    PDownConfigContent.getInstance().save();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("checkCert")
  public FullHttpResponse checkCert(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("status", AppUtil.checkIsInstalledCert());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("installCert")
  public FullHttpResponse installCert(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    boolean status;
    if (OsUtil.isUnix() || OsUtil.isWindowsXP()) {
      if (!AppUtil.checkIsInstalledCert()) {
        ExtensionCertUtil.buildCert(AppUtil.SSL_PATH, AppUtil.SUBJECT);
      }
      Desktop.getDesktop().open(new File(AppUtil.SSL_PATH));
      status = true;
    } else {
      //再检测一次，确保不重复安装
      if (!AppUtil.checkIsInstalledCert()) {
        if (ExtensionCertUtil.existsCert(AppUtil.SUBJECT)) {
          //存在无用证书需要卸载
          ExtensionCertUtil.uninstallCert(AppUtil.SUBJECT);
        }
        //生成新的证书
        ExtensionCertUtil.buildCert(AppUtil.SSL_PATH, AppUtil.SUBJECT);
        //安装
        ExtensionCertUtil.installCert(new File(AppUtil.CERT_PATH));
        //检测是否安装成功，可能点了取消就没安装成功
        status = AppUtil.checkIsInstalledCert();
      } else {
        status = true;
      }
    }
    data.put("status", status);
    if (status && !PDownProxyServer.isStart) {
      new Thread(() -> {
        try {
          AppUtil.startProxyServer();
        } catch (IOException e) {
          LOGGER.error("Start proxy server error", e);
        }
      }).start();
    }
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("copy")
  public FullHttpResponse copy(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable selection = null;
    if ("text".equalsIgnoreCase((String) map.get("type"))) {
      selection = new StringSelection((String) map.get("data"));
    }
    clipboard.setContents(selection, null);
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("updateExtensionSetting")
  public FullHttpResponse updateExtensionSetting(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> map = getJSONParams(request);
    String path = (String) map.get("path");
    Map<String, Object> setting = (Map<String, Object>) map.get("setting");
    ExtensionInfo extensionInfo = ExtensionContent.get()
        .stream()
        .filter(e -> e.getMeta().getPath().equals(path))
        .findFirst()
        .get();
    extensionInfo.getMeta().setSettings(setting).save();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("onResolve")
  public FullHttpResponse onResolve(Channel channel, FullHttpRequest request) throws Exception {
    HttpRequestForm taskRequest = getJSONParams(request, HttpRequestForm.class);
    //遍历扩展模块是否有对应的处理
    List<ExtensionInfo> extensionInfos = ExtensionContent.get();
    for (ExtensionInfo extensionInfo : extensionInfos) {
      if (extensionInfo.getMeta().isEnabled()) {
        if (extensionInfo.getHookScript() != null
            && !StringUtils.isEmpty(extensionInfo.getHookScript().getScript())) {
          Event event = extensionInfo.getHookScript().hasEvent(HookScript.EVENT_RESOLVE, taskRequest.getUrl());
          if (event != null) {
            try {
              //执行resolve方法
              Object result = ExtensionUtil.invoke(extensionInfo, event, taskRequest, false);
              if (result != null && !(result instanceof Undefined)) {
                ObjectMapper objectMapper = new ObjectMapper();
                String temp = objectMapper.writeValueAsString(result);
                TaskForm taskForm = objectMapper.readValue(temp, TaskForm.class);
                //有一个扩展解析成功的话直接返回
                return HttpHandlerUtil.buildJson(taskForm, Include.NON_DEFAULT);
              }
            } catch (Exception e) {
              LOGGER.error("An exception occurred while resolve()", e);
            }
          }
        }
      }
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  private Map<String, Object> getJSONParams(FullHttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
  }

  private <T> T getJSONParams(FullHttpRequest request, Class<T> clazz) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), clazz);
  }

}
