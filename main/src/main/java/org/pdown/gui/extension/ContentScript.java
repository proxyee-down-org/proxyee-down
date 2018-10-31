package org.pdown.gui.extension;

import java.util.Arrays;

public class ContentScript {

  private String[] matches;
  private String[] scripts;

  public String[] getMatches() {
    return matches;
  }

  public ContentScript setMatches(String[] matches) {
    this.matches = matches;
    return this;
  }

  public String[] getScripts() {
    return scripts;
  }

  public ContentScript setScripts(String[] scripts) {
    this.scripts = scripts;
    return this;
  }

  public boolean isMatch(String url) {
    if (matches != null
        && Arrays.stream(matches).anyMatch(m -> url.matches(m))) {
      return true;
    }
    return false;
  }
}
