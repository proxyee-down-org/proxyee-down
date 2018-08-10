package org.pdown.gui.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class ExtensionInfo {

  private String path;
  private String title;
  private double version;
  private String description;
  private List<ContentScript> contentScripts;

  public String getPath() {
    return path;
  }

  public ExtensionInfo setPath(String path) {
    this.path = path;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public ExtensionInfo setTitle(String title) {
    this.title = title;
    return this;
  }

  public double getVersion() {
    return version;
  }

  public ExtensionInfo setVersion(double version) {
    this.version = version;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public ExtensionInfo setDescription(String description) {
    this.description = description;
    return this;
  }

  public List<ContentScript> getContentScripts() {
    return contentScripts;
  }

  public ExtensionInfo setContentScripts(List<ContentScript> contentScripts) {
    this.contentScripts = contentScripts;
    return this;
  }

  public static class ContentScript {

    private String[] domains;
    private String[] matches;
    private String[] scripts;

    public String[] getDomains() {
      return domains;
    }

    public ContentScript setDomains(String[] domains) {
      this.domains = domains;
      return this;
    }

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
  }

  public static void main(String[] args) throws JsonProcessingException {
    ExtensionInfo extensionInfo = new ExtensionInfo();
    extensionInfo.setTitle("test");
    extensionInfo.setDescription("test desc");
    extensionInfo.setVersion(0.1);
    List<ContentScript> list = new ArrayList<>();
    list.add(new ContentScript()
        .setDomains(new String[]{"www.baidu.com"})
        .setMatches(new String[]{"^www.baidu.com$"})
        .setScripts(new String[]{"bdy.js"})
    );
    extensionInfo.setContentScripts(list);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValueAsString(extensionInfo);
    System.out.println(objectMapper.writeValueAsString(extensionInfo));
  }
}
