package org.pdown.gui.entity;

import java.io.Serializable;

public class PDownConfigInfo implements Serializable{

  private static final long serialVersionUID = 250452934883002540L;
  private String locale;
  //代理模式 0.不接管系统代理 1.由pdown接管系统代理
  private int proxyMode;

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
}
