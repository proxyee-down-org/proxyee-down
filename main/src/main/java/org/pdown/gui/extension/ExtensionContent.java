package org.pdown.gui.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.pdown.rest.util.PathUtil;

public class ExtensionContent {

  public static final String EXT_DIR = PathUtil.ROOT_PATH + File.separator + "extensions";
  private static final String EXT_MANIFEST = "manifest.json";

  private static List<ExtensionInfo> EXTENSION_INFO_LIST;
  //代理服务器域名通配符列表
  private static Set<String> PROXY_WILDCARDS;
  //需要嗅探下载的url正则表达式列表
  private static Set<String> SNIFF_REGEXS;

  public static void load() throws IOException {
    File file = new File(EXT_DIR);
    if (EXTENSION_INFO_LIST == null) {
      EXTENSION_INFO_LIST = new ArrayList<>();
    } else {
      EXTENSION_INFO_LIST.clear();
    }
    if (file.exists() && file.isDirectory()) {
      //加载所有已安装的扩展
      for (File extendDir : file.listFiles()) {
        if (extendDir.isDirectory()) {
          //读取manifest.json
          ExtensionInfo extensionInfo = parseExtensionDir(extendDir);
          if (extensionInfo != null) {
            EXTENSION_INFO_LIST.add(extensionInfo);
          }
        }
      }
      refresh();
    }
  }

  public synchronized static void refresh(String path) throws IOException {
    if (EXTENSION_INFO_LIST != null && path != null) {
      boolean match = false;
      for (int i = 0; i < EXTENSION_INFO_LIST.size(); i++) {
        ExtensionInfo extensionInfo = EXTENSION_INFO_LIST.get(i);
        if (path.equals(extensionInfo.getMeta().getPath())) {
          match = true;
          EXTENSION_INFO_LIST.set(i, parseExtensionDir(new File(EXT_DIR + path)));
          break;
        }
      }
      if (!match) {
        EXTENSION_INFO_LIST.add(parseExtensionDir(new File(EXT_DIR + path)));
      }
      refresh();
    }
  }

  public synchronized static void refresh() throws IOException {
    if (PROXY_WILDCARDS == null) {
      PROXY_WILDCARDS = new HashSet<>();
    } else {
      PROXY_WILDCARDS.clear();
    }
    if (SNIFF_REGEXS == null) {
      SNIFF_REGEXS = new HashSet<>();
    } else {
      SNIFF_REGEXS.clear();
    }
    if (EXTENSION_INFO_LIST != null) {
      for (ExtensionInfo extensionInfo : EXTENSION_INFO_LIST) {
        if (extensionInfo.getMeta().isEnabled()) {
          //读取需要代理的域名匹配符
          if (extensionInfo.getProxyWildcards() != null) {
            for (String wildcard : extensionInfo.getProxyWildcards()) {
              PROXY_WILDCARDS.add(wildcard.trim());
            }
          }
          //读取需要嗅探下载的url正则表达式
          if (extensionInfo.getSniffRegexs() != null) {
            for (String regex : extensionInfo.getSniffRegexs()) {
              SNIFF_REGEXS.add(regex.trim());
            }
          }
        }
      }
    }
  }

  private static ExtensionInfo parseExtensionDir(File extendDir) {
    ExtensionInfo extensionInfo = null;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      extensionInfo = objectMapper.readValue(new FileInputStream(extendDir + File.separator + EXT_MANIFEST), ExtensionInfo.class);
    } catch (IOException e) {
    }
    if (extensionInfo != null) {
      Meta meta = Meta.load(extendDir.getPath());
      extensionInfo.setMeta(meta);
    }
    return extensionInfo;
  }

  public static List<ExtensionInfo> get() {
    return EXTENSION_INFO_LIST;
  }

  public static Set<String> getProxyWildCards() {
    return PROXY_WILDCARDS;
  }

  public static Set<String> getSniffRegexs() {
    return SNIFF_REGEXS;
  }
}
