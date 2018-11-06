package org.pdown.gui.extension;

import java.util.List;

public class ExtensionConfig {

  //本地加载的扩展
  private List<String> localExtensions;

  public List<String> getLocalExtensions() {
    return localExtensions;
  }

  public void setLocalExtensions(List<String> localExtensions) {
    this.localExtensions = localExtensions;
  }
}
