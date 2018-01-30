package lee.study.down.mvc.form;

import lee.study.down.model.ConfigBaseInfo;
import lee.study.down.model.ConfigInfo;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigForm extends ConfigBaseInfo {

  private ProxyConfigForm secProxyConfig; //二级代理设置

  @Data
  public class ProxyConfigForm {

    private String proxyType;
    private String host;
    private int port;
    private String user;
    private String pwd;

    public ProxyConfig convert() {
      return new ProxyConfig(ProxyType.valueOf(proxyType), host, port, user, pwd);
    }
  }

  public ConfigInfo convert() {
    ConfigInfo configInfo = new ConfigInfo();
    BeanUtils.copyProperties(this, configInfo, new String[]{"secProxyConfig"});
    if (secProxyConfig != null) {
      configInfo.setSecProxyConfig(secProxyConfig.convert());
    }
    return configInfo;
  }
}
