package lee.study.down.constant;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.net.ssl.SSLException;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.dispatch.HttpDownHandleCallback;
import lee.study.down.util.PathUtil;

public class HttpDownConstant {

  public static String HOME_PATH;
  public static String TASK_RECORD_PATH;
  public static String CONFIG_PATH;
  public static SslContext clientSslContext;
  public static NioEventLoopGroup clientLoopGroup;
  public static HttpDownCallback httpDownCallback;

  static {
    HOME_PATH = PathUtil.ROOT_PATH;
    if ("1".equals(System.getProperty("exe4j"))) {  //exe4j中文路径特殊处理
      try {
        HOME_PATH = URLDecoder.decode(HOME_PATH, "UTF-8");
      } catch (UnsupportedEncodingException e) {
      }
    }
    TASK_RECORD_PATH = HOME_PATH + File.separator + "records.inf";
    CONFIG_PATH = HOME_PATH + File.separator + "config.inf";
    try {
      clientSslContext = SslContextBuilder.forClient()
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .build();
    } catch (SSLException e) {
      e.printStackTrace();
    }
    clientLoopGroup = new NioEventLoopGroup(1);
    httpDownCallback = new HttpDownHandleCallback();
  }
}
