package org.pdown.gui.extension.mitm.server;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.channel.Channel;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.entity.PDownConfigInfo;
import org.pdown.gui.extension.mitm.intercept.AjaxIntercept;
import org.pdown.gui.extension.mitm.intercept.CookieIntercept;
import org.pdown.gui.extension.mitm.intercept.ScriptIntercept;
import org.pdown.gui.extension.mitm.intercept.SniffIntercept;
import org.pdown.gui.extension.mitm.ssl.PDownCACertFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDownProxyServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(PDownProxyServer.class);

  private static volatile HttpProxyServer httpProxyServer;
  public static volatile boolean isStart = false;

  public static void start(int port) {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    //处理ssl
    config.setHandleSsl(true);
    //线程池数量都设置为1
    config.setBossGroupThreads(1);
    config.setWorkerGroupThreads(1);
    config.setProxyGroupThreads(1);
    httpProxyServer = new HttpProxyServer()
        .serverConfig(config)
        .proxyConfig(PDownConfigInfo.convert(PDownConfigContent.getInstance().get().getProxyConfig()))
        .caCertFactory(new PDownCACertFactory())
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new CookieIntercept());
            pipeline.addLast(new AjaxIntercept());
            pipeline.addLast(new ScriptIntercept());
            pipeline.addLast(new SniffIntercept());
          }
        })
        .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
          @Override
          public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
            LOGGER.warn("beforeCatch", cause);
          }

          @Override
          public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause) throws Exception {
            LOGGER.warn("afterCatch", cause);
          }
        });
    isStart = true;
    httpProxyServer.start(port);
  }

  public static void close() {
    if (httpProxyServer != null) {
      httpProxyServer.close();
    }
    isStart = false;
  }

}
