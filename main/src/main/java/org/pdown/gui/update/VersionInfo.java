package org.pdown.gui.update;

public class VersionInfo {
  private double version;
  private String path;

  public double getVersion() {
    return version;
  }

  public VersionInfo setVersion(double version) {
    this.version = version;
    return this;
  }

  public String getPath() {
    return path;
  }

  public VersionInfo setPath(String path) {
    this.path = path;
    return this;
  }
}
