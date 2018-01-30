package lee.study.down.model;

import java.io.Serializable;
import lee.study.proxyee.proxy.ProxyConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigInfo extends ConfigBaseInfo implements Serializable {

  private static final long serialVersionUID = 4780168673614933999L;

  private ProxyConfig secProxyConfig; //二级代理设置
}
