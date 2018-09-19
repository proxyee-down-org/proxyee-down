package org.pdown.gui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.boot.URLHttpDownBootstrapBuilder;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.proxy.ProxyConfig;
import org.pdown.core.proxy.ProxyType;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.mitm.server.PDownProxyServer;
import org.pdown.gui.extension.mitm.util.ExtensionCertUtil;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.rest.util.PathUtil;
import org.springframework.util.StringUtils;

public class AppUtil {

  public static final String SUBJECT = "ProxyeeDown CA";
  public static final String SSL_PATH = PathUtil.ROOT_PATH + File.separator + "ssl" + File.separator;
  public static final String CERT_PATH = SSL_PATH + "ca.crt";
  public static final String PRIVATE_PATH = SSL_PATH + ".ca_pri.der";

  /**
   * 证书和私钥文件都存在并且检测到系统安装了这个证书
   */
  public static boolean checkIsInstalledCert() throws Exception {
    return FileUtil.exists(CERT_PATH)
        && FileUtil.exists(PRIVATE_PATH)
        && ExtensionCertUtil.isInstalledCert(new File(CERT_PATH));
  }

  /**
   * 刷新系统PAC代理
   */
  public static void refreshPAC() throws IOException {
    if (PDownConfigContent.getInstance().get().getProxyMode() == 1) {
      ExtensionProxyUtil.enabledPACProxy("http://127.0.0.1:" + DownApplication.INSTANCE.API_PORT + "/pac/pdown.pac?t=" + System.currentTimeMillis());
    }
  }

  /**
   * 运行代理服务器
   */
  public static void startProxyServer() throws IOException {
    DownApplication.INSTANCE.PROXY_PORT = OsUtil.getFreePort(9999);
    PDownProxyServer.start(DownApplication.INSTANCE.PROXY_PORT);
  }

  /**
   * 下载http资源
   */
  public static void download(String url, String path) throws IOException {
    URL u = new URL(url);
    HttpURLConnection connection;
    ProxyConfig proxyConfig = PDownConfigContent.getInstance().get().getProxyConfig();
    if (proxyConfig != null) {
      Type type;
      if (proxyConfig.getProxyType() == ProxyType.HTTP) {
        type = Type.HTTP;
      } else {
        type = Type.SOCKS;
      }
      if (!StringUtils.isEmpty(proxyConfig.getUser())) {
        Authenticator authenticator = new Authenticator() {
          public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(proxyConfig.getUser(),
                proxyConfig.getPwd() == null ? null : proxyConfig.getPwd().toCharArray());
          }
        };
        Authenticator.setDefault(authenticator);
      }
      Proxy proxy = new Proxy(type, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
      connection = (HttpURLConnection) u.openConnection(proxy);
    } else {
      connection = (HttpURLConnection) u.openConnection();
    }
    connection.setConnectTimeout(30000);
    connection.setReadTimeout(0);
    File file = new File(path);
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

  /**
   * 使用pdown-core多连接下载http资源
   */
  public static HttpDownBootstrap fastDownload(String url, File file, HttpDownCallback callback) throws IOException {
    HttpDownBootstrap httpDownBootstrap = new URLHttpDownBootstrapBuilder(url, null, null)
        .callback(callback)
        .downConfig(new HttpDownConfigInfo().setFilePath(file.getParent()).setConnections(64))
        .response(new HttpResponseInfo().setFileName(file.getName()))
        .proxyConfig(PDownConfigContent.getInstance().get().getProxyConfig())
        .build();
    httpDownBootstrap.start();
    return httpDownBootstrap;
  }
}
