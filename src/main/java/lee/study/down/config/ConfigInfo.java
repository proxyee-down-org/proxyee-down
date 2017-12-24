package lee.study.down.config;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfo implements Serializable{

  private static final long serialVersionUID = 7824653416503878396L;
  private boolean first;  //是否第一次打开
  private int localPort;  //代理端口号
  private int localProxyType; //1.全局代理 2.局部代理
  private boolean secProxyEnabled;  //二级代理开关
  private ProxyInfo proxyInfo;  //二级代理信息
}
