package lee.study.down;

import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.server.HttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownProxyServer {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownProxyServer.class);

  private HttpDownInterceptFactory interceptFactory;

  private HttpProxyServer proxyServer;
  private ProxyConfig proxyConfig;

  public HttpDownProxyServer(ProxyConfig proxyConfig, HttpDownInterceptFactory interceptFactory) {
    this.proxyConfig = proxyConfig;
    this.interceptFactory = interceptFactory;
    proxyServer = new HttpProxyServer();
  }

  public void setProxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
  }

  public void start(int port) {
    LOGGER.debug("HttpDownProxyServer listen " + port + "\tproxyConfig:" + proxyConfig);
    proxyServer.proxyConfig(proxyConfig);
    //监听http下载请求
    proxyServer.proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new CertDownIntercept());
        pipeline.addLast(new BdyIntercept());
        pipeline.addLast(new HttpDownSniffIntercept());
        HttpProxyIntercept downIntercept = interceptFactory.create();
        if (downIntercept != null) {
          pipeline.addLast(downIntercept);
        }
      }
    }).start(port);
  }

  public void close() {
    proxyServer.close();
  }

}
