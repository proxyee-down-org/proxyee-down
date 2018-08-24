package org.pdown.gui.entity;

import java.io.Serializable;
import java.util.List;

public class PDownConfigInfo implements Serializable {

  private static final long serialVersionUID = 250452934883002540L;
  //客户端语言
  private String locale;
  //代理模式 0.不接管系统代理 1.由pdown接管系统代理
  private int proxyMode;
  //插件文件服务器(用于下载插件相关文件)
  private List<String> extFileServers;

  public String getLocale() {
    return locale;
  }

  public PDownConfigInfo setLocale(String locale) {
    this.locale = locale;
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
}
