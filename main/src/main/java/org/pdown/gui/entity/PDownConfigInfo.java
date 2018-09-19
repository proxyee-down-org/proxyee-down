package org.pdown.gui.entity;

import java.io.Serializable;
import java.util.List;
import org.pdown.core.proxy.ProxyConfig;

public class PDownConfigInfo implements Serializable {

  private static final long serialVersionUID = 250452934883002540L;
  //客户端语言
  private String locale;
  //UI模式 0.浏览器模式 1.GUI模式
  private int uiMode = 1;
  //代理模式 0.不接管系统代理 1.由pdown接管系统代理
  private int proxyMode;
  //插件文件服务器(用于下载插件相关文件)
  private List<String> extFileServers;
  //检测更新频率 0.从不 1.一周检查一次 2.每次打开检查
  private int updateCheckRate = 2;
  //最后一次检查更新时间
  private long lastUpdateCheck;
  //前置代理
  private ProxyConfig proxyConfig;

  public String getLocale() {
    return locale;
  }

  public PDownConfigInfo setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  public int getUiMode() {
    return uiMode;
  }

  public PDownConfigInfo setUiMode(int uiMode) {
    this.uiMode = uiMode;
    return this;
  }

  public int getProxyMode() {
    return proxyMode;
  }

  public PDownConfigInfo setProxyMode(int proxyMode) {
    this.proxyMode = proxyMode;
    return this;
  }

  public List<String> getExtFileServers() {
    return extFileServers;
  }

  public PDownConfigInfo setExtFileServers(List<String> extFileServers) {
    this.extFileServers = extFileServers;
    return this;
  }

  public int getUpdateCheckRate() {
    return updateCheckRate;
  }

  public PDownConfigInfo setUpdateCheckRate(int updateCheckRate) {
    this.updateCheckRate = updateCheckRate;
    return this;
  }

  public long getLastUpdateCheck() {
    return lastUpdateCheck;
  }

  public PDownConfigInfo setLastUpdateCheck(long lastUpdateCheck) {
    this.lastUpdateCheck = lastUpdateCheck;
    return this;
  }

  public ProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public PDownConfigInfo setProxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

  public static com.github.monkeywie.proxyee.proxy.ProxyConfig convert(ProxyConfig proxyConfig) {
    if (proxyConfig == null) {
      return null;
    }
    return new com.github.monkeywie.proxyee.proxy.ProxyConfig(
        com.github.monkeywie.proxyee.proxy.ProxyType.valueOf(proxyConfig.getProxyType().name()),
        proxyConfig.getHost(),
        proxyConfig.getPort(),
        proxyConfig.getUser(),
        proxyConfig.getPwd());
  }
}
