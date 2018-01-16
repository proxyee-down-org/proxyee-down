package lee.study.down.mvc.form;

import lee.study.down.model.ConfigInfo;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyType;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class ConfigForm {

  private boolean guideFlag;  //是否需要新手引导教程
  private int proxyPort;  //代理端口号
  private int timeout;  //超时重试时间
  private int connections;  //默认分段数
  private boolean secProxyEnable; //二级代理开关
  private ProxyConfigForm secProxyConfig; //二级代理设置
  private String lastPath;  //最后保存文件的路径

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
