package org.pdown.gui.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtensionContent {

//  public static final String EXT_DIR = PathUtil.ROOT_PATH + File.separator + "extensions";
  public static final String EXT_DIR = "E:\\study\\proxyee-down-extension";
  private static final String EXT_MANIFEST = "manifest.json";

  private static List<ExtensionInfo> EXTENSION_INFO_LIST;
  private static Set<String> WILDCARDS;

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
      refreshProxyWildcards();
    }
  }

  public synchronized static void refreshExtensionInfo(String path) throws IOException {
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
      refreshProxyWildcards();
    }
  }

  public synchronized static void refreshProxyWildcards() throws IOException {
    if (WILDCARDS == null) {
      WILDCARDS = new HashSet<>();
    } else {
      WILDCARDS.clear();
    }
    if (EXTENSION_INFO_LIST != null) {
      for (ExtensionInfo extensionInfo : EXTENSION_INFO_LIST) {
        if (extensionInfo.getMeta().isEnabled() && extensionInfo.getProxyWildcards() != null) {
          //读取需要代理的域名匹配符
          for (String wildcard : extensionInfo.getProxyWildcards()) {
            WILDCARDS.add(wildcard.trim());
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

  public static Set<String> getWildCards() {
    return WILDCARDS;
  }
}
