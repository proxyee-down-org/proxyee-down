package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.util.OsUtil;

public class HttpDownBootstrapFactory {

  private static volatile TimeoutCheckTask timeoutCheck;

  public static AbstractHttpDownBootstrap create(HttpDownInfo httpDownInfo, int retryCount,
      SslContext clientSslContext, NioEventLoopGroup clientLoopGroup, HttpDownCallback callback) {
    if (timeoutCheck == null) {
      synchronized (HttpDownBootstrapFactory.class) {
        if (timeoutCheck == null) {
          timeoutCheck = new TimeoutCheckTask();
          timeoutCheck.start();
        }
      }
    }
    AbstractHttpDownBootstrap bootstrap;
    if (OsUtil.is64()) {
      bootstrap = new X64HttpDownBootstrap(httpDownInfo, retryCount, clientSslContext,
          clientLoopGroup, callback, timeoutCheck);
    } else {
      bootstrap = new X86HttpDownBootstrap(httpDownInfo, retryCount, clientSslContext,
          clientLoopGroup, callback, timeoutCheck);
    }
    timeoutCheck.addBoot(bootstrap);
    return bootstrap;
  }
}
