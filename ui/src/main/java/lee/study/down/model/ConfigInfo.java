package lee.study.down.model;

import java.io.Serializable;
import lee.study.proxyee.proxy.ProxyConfig;
import lombok.Data;

@Data
public class ConfigInfo implements Serializable{

  private static final long serialVersionUID = 4780168673614933999L;

  private boolean guideFlag;  //是否需要新手引导教程
  private int proxyPort;  //代理端口号
  private boolean secProxyEnable; //二级代理开关
  private ProxyConfig secProxyConfig; //二级代理设置
  private String lastPath;  //最后保存文件的路径
}
