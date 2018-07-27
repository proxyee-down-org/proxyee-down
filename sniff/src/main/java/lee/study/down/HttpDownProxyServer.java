package lee.study.down;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.server.HttpProxyCACertFactory;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.channel.Channel;
import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownProxyServer {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownProxyServer.class);

  private HttpDownInterceptFactory interceptFactory;

  private HttpProxyServer proxyServer;
  private ProxyConfig proxyConfig;

  public HttpDownProxyServer(HttpProxyCACertFactory caCertFactory, ProxyConfig proxyConfig,
      HttpDownInterceptFactory interceptFactory) {
    this.proxyConfig = proxyConfig;
    this.interceptFactory = interceptFactory;
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    config.setBossGroupThreads(1);
    config.setWorkerGroupThreads(1);
    config.setProxyGroupThreads(1);
    this.proxyServer = new HttpProxyServer()
        .caCertFactory(caCertFactory)
        .serverConfig(config);
  }

  public void setProxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
  }

  public void start(int port) {
    LOGGER.debug("HttpDownProxyServer listen " + port + "\tproxyConfig:" + proxyConfig);
    //监听http下载请求
    proxyServer.proxyConfig(proxyConfig);
    proxyServer.proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new BdyIntercept());
        pipeline.addLast(new HttpDownSniffIntercept());
        HttpProxyIntercept downIntercept = interceptFactory.create();
        if (downIntercept != null) {
          pipeline.addLast(downIntercept);
        }
      }
    })
        .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
          @Override
          public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
            LOGGER.warn("beforeCatch:", cause);
          }

          @Override
          public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
              throws Exception {
            LOGGER.warn("afterCatch:", cause);
          }
        }).start(port);
  }

  public void close() {
    proxyServer.close();
  }

}
