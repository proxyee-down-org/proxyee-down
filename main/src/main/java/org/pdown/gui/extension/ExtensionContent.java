package org.pdown.gui.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.pdown.gui.extension.ExtensionInfo.ContentScript;
import org.pdown.rest.util.PathUtil;

public class ExtensionContent {

  private static final String EXT_DIR = "extends";
  private static final String EXT_MANIFEST = "manifest.json";

  private static List<ExtensionInfo> EXTENSION_INFO_LIST;
  private static Set<String> DOMAINS;

  public synchronized static void load() {
//    File file = new File(PathUtil.ROOT_PATH + File.separator + EXT_DIR);
    File file = new File("E:\\exts");
    if (file.exists() && file.isDirectory()) {
      if (EXTENSION_INFO_LIST == null) {
        EXTENSION_INFO_LIST = new ArrayList<>();
        DOMAINS = new HashSet<>();
      } else {
        EXTENSION_INFO_LIST.clear();
        DOMAINS.clear();
      }
      for (File extendDir : file.listFiles()) {
        if (extendDir.isDirectory()) {
          ExtensionInfo extensionInfo = parseExtendDir(extendDir);
          if (extensionInfo != null) {
            extensionInfo.setPath(extendDir.getPath());
            if (extensionInfo.getContentScripts() != null && extensionInfo.getContentScripts().size() > 0) {
              for (ContentScript contentScript : extensionInfo.getContentScripts()) {
                if (contentScript.getDomains() != null && contentScript.getDomains().length > 0) {
                  for (String domain : contentScript.getDomains()) {
                    DOMAINS.add(domain.trim());
                  }
                }
              }
            }
            EXTENSION_INFO_LIST.add(extensionInfo);
          }
        }
      }
    }
  }

  private static ExtensionInfo parseExtendDir(File extendDir) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(new FileInputStream(extendDir + File.separator + EXT_MANIFEST), ExtensionInfo.class);
    } catch (IOException e) {

    }
    return null;
  }

  public synchronized static List<ExtensionInfo> get() {
    return EXTENSION_INFO_LIST;
  }

  public synchronized static Set<String> getDomains() {
    return DOMAINS;
  }
}
