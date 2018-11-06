package org.pdown.gui.util;

import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtil {

  private static Map<String, Object> map = null;

  static {
    Yaml yaml = new Yaml();
    map = yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yml"));
    String active = getString("spring.profiles.active");
    merge(map, yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application-" + active + ".yml")));
  }

  public static String getString(String key) {
    return getString(map, key);
  }

  public static boolean getBoolean(String key) {
    return getBoolean(map, key);
  }

  public static int getInt(String key) {
    return getInt(map, key);
  }

  static Object get(Map<String, Object> map, String key) {
    String[] keyArray = key.split("\\.");
    if (keyArray.length == 1) {
      return map.get(key);
    } else {
      for (int i = 0; i < keyArray.length - 1; i++) {
        map = (Map<String, Object>) get(map, keyArray[i]);
      }
      return map.get(keyArray[keyArray.length - 1]);
    }
  }

  private static void merge(Map<String, Object> map1, Map<String, Object> map2) {
    for (Entry<String, Object> entry : map2.entrySet()) {
      if (map1.containsKey(entry.getKey())) {
        if (entry.getValue() instanceof Map && map2.get(entry.getKey()) instanceof Map) {
          merge(map1, (Map<String, Object>) map2.get(entry.getKey()));
        } else {
          map1.put(entry.getKey(), map2.get(entry.getKey()));
        }
      } else {
        map1.put(entry.getKey(), map2.get(entry.getKey()));
      }
    }
  }

  private static String getString(Map<String, Object> map, String key) {
    return String.valueOf(get(map, key));
  }

  private static boolean getBoolean(Map<String, Object> map, String key) {
    return Boolean.valueOf(get(map, key).toString());
  }

  private static int getInt(Map<String, Object> map, String key) {
    return Integer.valueOf(get(map, key).toString());
  }

}
