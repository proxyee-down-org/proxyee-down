package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.util.OsUtil;

public class HttpDownBootstrapFactory {

  public static AbstractHttpDownBootstrap create(HttpDownInfo httpDownInfo,
      SslContext clientSslContext, NioEventLoopGroup clientLoopGroup, HttpDownCallback callback) {
    if (OsUtil.is64()) {
      return new X64HttpDownBootstrap(httpDownInfo, clientSslContext, clientLoopGroup, callback);
    } else {
      return new X86HttpDownBootstrap(httpDownInfo, clientSslContext, clientLoopGroup, callback);
    }
  }
}
