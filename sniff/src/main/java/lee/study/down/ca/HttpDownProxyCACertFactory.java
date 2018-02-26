package lee.study.down.ca;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import lee.study.proxyee.crt.CertUtil;
import lee.study.proxyee.server.HttpProxyCACertFactory;

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
