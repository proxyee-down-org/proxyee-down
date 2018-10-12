package org.pdown.gui.extension;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.pdown.rest.util.ContentUtil;

public class Meta {

  public transient static final String CONFIG_FILE = ".ext_data/.config.dat";

  private transient String path;
  private transient String fullPath;
  private boolean enabled = true;
  private boolean local = true;
  private Map<String, Object> data;

  public String getPath() {
    return path;
  }

  public Meta setPath(String path) {
    this.path = path;
    return this;
  }

  public String getFullPath() {
    return fullPath;
  }

  public Meta setFullPath(String fullPath) {
    this.fullPath = fullPath;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Meta setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public Meta setData(Map<String, Object> data) {
    this.data = data;
    return this;
  }

  public boolean isLocal() {
    return local;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public void save() {
    try {
      ContentUtil.save(this, getFullPath() + File.separator + CONFIG_FILE, true);
    } catch (IOException e) {
    }
  }

  public static Meta load(String path) {
    Meta meta = null;
    try {
      meta = ContentUtil.get(path + File.separator + CONFIG_FILE, Meta.class);
    } catch (IOException e) {
    }
    if (meta == null) {
      meta = new Meta();
    }
    meta.setPath("/" + new File(path).getName());
    meta.setFullPath(path);
    return meta;
  }
}
