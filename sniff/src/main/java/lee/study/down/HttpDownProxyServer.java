package lee.study.down;

import io.netty.channel.Channel;
import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import lee.study.proxyee.exception.HttpProxyExceptionHandle;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.server.HttpProxyCACertFactory;
import lee.study.proxyee.server.HttpProxyServer;
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

    this.proxyServer = new HttpProxyServer(caCertFactory);
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
