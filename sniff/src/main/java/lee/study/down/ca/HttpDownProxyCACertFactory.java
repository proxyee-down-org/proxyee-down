package lee.study.down.ca;

import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.server.HttpProxyCACertFactory;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class HttpDownProxyCACertFactory implements HttpProxyCACertFactory {

  private String crtPath;
  private String priKeyPath;

  public HttpDownProxyCACertFactory(String crtPath, String priKeyPath) {
    this.crtPath = crtPath;
    this.priKeyPath = priKeyPath;
  }

  @Override
  public X509Certificate getCACert() throws Exception {
    return CertUtil.loadCert(crtPath);
  }

  @Override
  public PrivateKey getCAPriKey() throws Exception {
    return CertUtil.loadPriKey(priKeyPath);
  }
}
