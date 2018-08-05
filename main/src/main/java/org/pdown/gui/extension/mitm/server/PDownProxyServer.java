package org.pdown.gui.extension.mitm.server;

import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import org.pdown.gui.extension.mitm.ssl.PDownCACertFactory;

public class PDownProxyServer {

  private static HttpProxyServer httpProxyServer;

  public static void start(int port) {
    if (httpProxyServer == null) {
      HttpProxyServerConfig config = new HttpProxyServerConfig();
      //处理ssl
      config.setHandleSsl(true);
      //线程池数量都设置为1
      config.setBossGroupThreads(1);
      config.setWorkerGroupThreads(1);
      config.setProxyGroupThreads(1);
      httpProxyServer = new HttpProxyServer()
          .serverConfig(config)
          .caCertFactory(new PDownCACertFactory());
    }
    httpProxyServer.start(port);
  }

  public static void close() {
    httpProxyServer.close();
  }

  public static void setProxyConfig(ProxyConfig proxyConfig) {
    httpProxyServer.proxyConfig(proxyConfig);
  }

}
