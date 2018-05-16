package lee.study.down.constant;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.File;
import javax.net.ssl.SSLException;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.dispatch.HttpDownHandleCallback;
import lee.study.down.util.PathUtil;

public class HttpDownConstant {

  public final static String HOME_PATH = PathUtil.ROOT_PATH;
  public final static String TASK_RECORD_PATH = HOME_PATH + ".records.inf";
  public final static String CONFIG_PATH = HOME_PATH + "config.inf";

  public final static String CA_SUBJECT = "ProxyeeRoot";
  public final static String SSL_PATH = HOME_PATH + "ssl" + File.separator;
  public final static String CA_CERT_PATH = SSL_PATH + "ca.crt";
  public final static String CA_PRI_PATH = SSL_PATH + ".ca_pri.der";

  public static SslContext clientSslContext;
  public static NioEventLoopGroup clientLoopGroup;
  public static HttpDownCallback httpDownCallback;

  static {
    try {
      clientSslContext = SslContextBuilder.forClient()
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .build();
    } catch (SSLException e) {
      e.printStackTrace();
    }
    clientLoopGroup = new NioEventLoopGroup();
    httpDownCallback = new HttpDownHandleCallback();
  }
}
