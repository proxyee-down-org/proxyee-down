package lee.study.down.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

  private static Properties prop;

  static {
    InputStream input = null;
    InputStream activeInput = null;
    try {
      input = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("application.properties");
      prop = new Properties();
      prop.load(input);
      String active = prop.getProperty("spring.profiles.active", "prd");
      activeInput = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("application-" + active + ".properties");
      prop.load(activeInput);
    } catch (IOException e) {
      throw new RuntimeException("config init", e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (activeInput != null) {
        try {
          activeInput.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String getValue(String key) {
    return prop.getProperty(key);
  }

  public static void setValue(String key, Object value) {
    prop.setProperty(key, value+"");
  }

  public static void main(String[] args) {

  }

}
