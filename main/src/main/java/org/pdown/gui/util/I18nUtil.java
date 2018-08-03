package org.pdown.gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.pdown.gui.content.PDownConfigContent;
import org.yaml.snakeyaml.Yaml;

public class I18nUtil {

  private static Map<String, Map<String, Object>> map;
  private static String locale = PDownConfigContent.getInstance().get().getLocale();

  static {
    Yaml yaml = new Yaml();
    map = new HashMap<>();
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      URL url = classLoader.getResource("i18n");
      URLConnection connection = url.openConnection();
      if (connection instanceof JarURLConnection) {
        JarURLConnection jarURLConnection = (JarURLConnection) connection;
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          map.put(takeLocale(entry.getName()), yaml.load(jarFile.getInputStream(entry)));
        }
        jarFile.close();
      } else {
        File dir = new File(url.getPath());
        for (File message : dir.listFiles()) {
          map.put(takeLocale(message.getName()), yaml.load(new FileInputStream(message)));
        }
      }
    } catch (IOException e) {
    }

//    map = yaml.load(Thread.currentThread().getContextClassLoader().getResource("i18n"));
//    String active = getString("spring.profiles.active");
//    merge(map, yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application-" + active + ".yml")));
  }

  private static String takeLocale(String name) {
    return name.substring(name.lastIndexOf("_") + 1, name.length() - 4);
  }

  public static String getLocale() {
    return locale;
  }

  public static void setLocale(String locale) {
    I18nUtil.locale = locale;
  }


  public static String getMessage(String key, Object... args) {
    if (map == null || map.size() == 0) {
      return key;
    }
    Map<String, Object> localeMap = map.get(locale);
    if (localeMap == null) {
      return key;
    }
    return MessageFormat.format(ConfigUtil.get(localeMap, key).toString(), args);
  }

  public static void main(String[] args) {
    setLocale("en-US");
    System.out.println(getMessage("gui.alert.startError", "1111"));
  }
}
