package org.pdown.gui.extension;

import java.util.List;

public class ExtensionInfo {

  private String title; //扩展名称
  private double version; //扩展版本号
  private String homepage; //扩展主页
  private String description; //扩展描述
  private List<String> proxyWildcards;  //扩展生效配置的域名通配符列表
  private List<String> sniffRegexs;  //扩展嗅探下载的url正则表达式列表
  private List<ContentScript> contentScripts;
  private HookScript hookScript;  //下载状态变更时触发的钩子函数脚本
  private List<Setting> settings; //扩展设置选项
  private Meta meta;

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

  public String getHomepage() {
    return homepage;
  }

  public ExtensionInfo setHomepage(String homepage) {
    this.homepage = homepage;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public ExtensionInfo setDescription(String description) {
    this.description = description;
    return this;
  }

  public List<String> getProxyWildcards() {
    return proxyWildcards;
  }

  public ExtensionInfo setProxyWildcards(List<String> proxyWildcards) {
    this.proxyWildcards = proxyWildcards;
    return this;
  }

  public List<String> getSniffRegexs() {
    return sniffRegexs;
  }

  public ExtensionInfo setSniffRegexs(List<String> sniffRegexs) {
    this.sniffRegexs = sniffRegexs;
    return this;
  }

  public List<ContentScript> getContentScripts() {
    return contentScripts;
  }

  public ExtensionInfo setContentScripts(List<ContentScript> contentScripts) {
    this.contentScripts = contentScripts;
    return this;
  }

  public HookScript getHookScript() {
    return hookScript;
  }

  public ExtensionInfo setHookScript(HookScript hookScript) {
    this.hookScript = hookScript;
    return this;
  }

  public List<Setting> getSettings() {
    return settings;
  }

  public ExtensionInfo setSettings(List<Setting> settings) {
    this.settings = settings;
    return this;
  }

  public Meta getMeta() {
    return meta;
  }

  public ExtensionInfo setMeta(Meta meta) {
    this.meta = meta;
    return this;
  }
}
