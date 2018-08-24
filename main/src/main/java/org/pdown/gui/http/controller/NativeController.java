package org.pdown.gui.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.awt.Desktop;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.com.Components;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.mitm.util.ExtensionCertUtil;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.extension.util.ExtensionUtil;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.gui.util.ExecUtil;
import org.pdown.rest.util.PathUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("native")
public class NativeController {

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

  @RequestMapping("getInitConfig")
  public FullHttpResponse getInitConfig(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    //语言
    data.put("locale", PDownConfigContent.getInstance().get().getLocale());
    //扩展商店请求地址
    data.put("extServer", ConfigUtil.getString("extServer"));
    //扩展下载服务器列表
    data.put("extFileServers", PDownConfigContent.getInstance().get().getExtFileServers());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("getLocale")
  public FullHttpResponse getLocale(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("locale", PDownConfigContent.getInstance().get().getLocale());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("setLocale")
  public FullHttpResponse setLocale(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
    String locale = map.get("locale");
    if (!StringUtils.isEmpty(locale)) {
      PDownConfigContent.getInstance().get().setLocale(locale);
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  @RequestMapping("showFile")
  public FullHttpResponse showFile(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
    String path = map.get("path");
    if (!StringUtils.isEmpty(path)) {
      File file = new File(path);
      if (!file.exists() || OsUtil.isUnix()) {
        Desktop.getDesktop().open(file.getParentFile());
      } else if (OsUtil.isWindows()) {
        ExecUtil.execSync("explorer.exe", "/select,", file.getPath());
      } else if (OsUtil.isMac()) {
        ExecUtil.execSync("open", "-R", file.getPath());
      }
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  /**
   * 获取已安装的插件列表
   */
  @RequestMapping("getExtensions")
  public FullHttpResponse getExtensions(Channel channel, FullHttpRequest request) throws Exception {
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

  private FullHttpResponse extensionCommon(FullHttpRequest request, boolean isUpdate) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
    String server = (String) map.get("server");
    String path = (String) map.get("path");
    String files = (String) map.get("files");
    if (isUpdate) {
      ExtensionUtil.update(server, path, files);
    } else {
      ExtensionUtil.install(server, path, files);
    }
    //刷新扩展content
    ExtensionContent.refreshExtensionInfo(path);
    //刷新系统pac代理
    if (PDownConfigContent.getInstance().get().getProxyMode() == 1) {
      ExtensionProxyUtil.enabledPACProxy("http://127.0.0.1:" + DownApplication.API_PORT + "/pac/pdown.pac?t=" + System.currentTimeMillis());
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  /**
   * 启用或禁用插件
   */
  @RequestMapping("toggleExtension")
  public FullHttpResponse toggleExtension(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
    String path = (String) map.get("path");
    boolean enabled = (boolean) map.get("enabled");
    ExtensionContent.get()
        .stream()
        .filter(extensionInfo -> extensionInfo.getMeta().getPath().equals(path))
        .findFirst()
        .get()
        .getMeta()
        .setEnabled(enabled)
        .save();
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
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
    int mode = (int) map.get("mode");
    //修改系统代理
    if (mode == 1) {
      ExtensionProxyUtil.enabledPACProxy("http://127.0.0.1:" + DownApplication.API_PORT + "/pac/pdown.pac?t=" + System.currentTimeMillis());
    } else {
      ExtensionProxyUtil.disabledProxy();
    }
    PDownConfigContent.getInstance().get().setProxyMode(mode);
    PDownConfigContent.getInstance().save();
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  private static final String SUBJECT = "ProxyeeDown CA";
  private static final String SSL_PATH = PathUtil.ROOT_PATH + File.separator + "ssl" + File.separator;
  private static final String CERT_PATH = SSL_PATH + "ca.crt";
  private static final String PRIVATE_PATH = SSL_PATH + ".ca_pri.der";

  @RequestMapping("checkCert")
  public FullHttpResponse checkCert(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("status", checkIsInstalledCert());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("installCert")
  public FullHttpResponse installCert(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    //再检测一次，确保不重复安装
    if (!checkIsInstalledCert()) {
      if (ExtensionCertUtil.existsCert(SUBJECT)) {
        //存在无用证书需要卸载
        ExtensionCertUtil.existsCert(SUBJECT);
      }
      //生成新的证书
      ExtensionCertUtil.buildCert(SSL_PATH, SUBJECT);
      //安装
      ExtensionCertUtil.installCert(new File(CERT_PATH));
      //检测是否安装成功，可能点了取消就没安装成功
      data.put("status", checkIsInstalledCert());
    } else {
      data.put("status", true);
    }
    return HttpHandlerUtil.buildJson(data);
  }

  //证书和私钥文件都存在并且检测到系统安装了这个证书
  private boolean checkIsInstalledCert() throws Exception {
    return FileUtil.exists(CERT_PATH)
        && FileUtil.exists(PRIVATE_PATH)
        && ExtensionCertUtil.isInstalledCert(new File(CERT_PATH));
  }
}
