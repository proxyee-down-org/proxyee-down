package lee.study.down.config;

import java.io.Serializable;
import lee.study.proxyee.proxy.ProxyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyInfo implements Serializable{

  private static final long serialVersionUID = -653176826637665476L;
  private ProxyType type;
  private String host;
  private int port;
  private String user;
  private String pwd;
}
