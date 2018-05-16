package lee.study.down.boot;

import io.netty.handler.ssl.SslContext;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.HttpDownInfo;

public class HttpDownBootstrapFactory {

  private static volatile TimeoutCheckTask timeoutCheck;

  public static AbstractHttpDownBootstrap create(HttpDownInfo httpDownInfo, int retryCount, SslContext clientSslContext, HttpDownCallback callback) {
    if (timeoutCheck == null) {
      synchronized (HttpDownBootstrapFactory.class) {
        if (timeoutCheck == null) {
          timeoutCheck = new TimeoutCheckTask();
          timeoutCheck.start();
        }
      }
    }
    AbstractHttpDownBootstrap bootstrap = new HttpDownBootstrap(httpDownInfo, retryCount, clientSslContext, callback, timeoutCheck);
    timeoutCheck.addBoot(bootstrap);
    return bootstrap;
  }
}
