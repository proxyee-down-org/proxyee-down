package lee.study.down;

import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.intercept.common.HttpDownInterceptFactory;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;

public class HttpDownProxyServer {

  private int port;
  private HttpDownInterceptFactory interceptFactory;

  private HttpProxyServer proxyServer;

  public HttpDownProxyServer(int port, HttpDownInterceptFactory interceptFactory) {
    this.port = port;
    this.interceptFactory = interceptFactory;
    proxyServer = new HttpProxyServer();
  }

  public void start() {
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

}
