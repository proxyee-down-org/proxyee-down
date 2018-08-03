package org.pdown.gui.entity;

import java.io.Serializable;

public class PDownConfigInfo implements Serializable{

  private static final long serialVersionUID = 250452934883002540L;
  private String locale = "zh-CN";

  public String getLocale() {
    return locale;
  }

  public PDownConfigInfo setLocale(String locale) {
    this.locale = locale;
    return this;
  }
}
