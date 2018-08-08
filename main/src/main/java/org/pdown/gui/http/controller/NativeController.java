package org.pdown.gui.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import org.pdown.core.util.FileUtil;
import org.pdown.gui.com.Components;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.mitm.util.ExtensionCertUtil;
import org.pdown.gui.http.util.HttpHandlerUtil;
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

  @RequestMapping("getLocale")
  public FullHttpResponse getLocale(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("locale", PDownConfigContent.getInstance().get().getLocale());
    return HttpHandlerUtil.buildJson(data);
  }

  @RequestMapping("setLocale")
  public FullHttpResponse setLocale(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(request.content().toString(Charset.defaultCharset()), Map.class);
    String locale = map.get("locale");
    if (!StringUtils.isEmpty(locale)) {
      PDownConfigContent.getInstance().get().setLocale(locale);
    }
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
