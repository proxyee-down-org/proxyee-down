package lee.study;

import io.netty.channel.nio.NioEventLoopGroup;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import lee.study.intercept.HttpDownIntercept;
import lee.study.model.HttpDownModel;
import lee.study.proxyee.crt.CertUtil;
import lee.study.proxyee.server.HttpProxyServer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HttpDownServer {

  public static List<HttpDownModel> downContent = new ArrayList<>();
  public static NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);

  private void start(int port) {
    new HttpProxyServer().proxyInterceptFactory(() -> new HttpDownIntercept()).start(port);
  }

  public static void main(String[] args) throws Exception {
    /*Security.addProvider(new BouncyCastleProvider());
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    KeyPair keyPair = CertUtil.genKeyPair();
    PublicKey serverPubKey = keyPair.getPublic();
    X509Certificate certificate = CertUtil.genCert(CertUtil.getSubject(classLoader.getResourceAsStream("ca.crt")), serverPubKey,
        CertUtil.loadPriKey(classLoader.getResourceAsStream("ca_private.pem")), "localhost","127.0.0.1");
    Files.write(Paths.get("F:/test.crt"),certificate.getEncoded());*/
    SpringApplication.run(HttpDownServer.class, args);
    new HttpDownServer().start(9999);
  }
}
