package org.pdown.gui.extension;

import java.util.Map;

public class Setting {

  private String name;
  private String title;
  private String type;
  private Object value;
  private String description;
  private boolean isMultiple;
  private Map<String, Object> options;

  public String getName() {
    return name;
  }

  public Setting setName(String name) {
    this.name = name;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Setting setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getType() {
    return type;
  }

  public Setting setType(String type) {
    this.type = type;
    return this;
  }

  public Object getValue() {
    return value;
  }

  public Setting setValue(Object value) {
    this.value = value;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Setting setDescription(String description) {
    this.description = description;
    return this;
  }

  public boolean isMultiple() {
    return isMultiple;
  }

  public Setting setMultiple(boolean multiple) {
    isMultiple = multiple;
    return this;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public Setting setOptions(Map<String, Object> options) {
    this.options = options;
    return this;
  }
}
