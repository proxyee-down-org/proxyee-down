package lee.study.down.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtil {

  public static String ROOT_PATH;

  static {
    String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    Pattern pattern = Pattern.compile("^file:/(.*/)[^/]*\\.jar!/BOOT-INF/classes!/$");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      ROOT_PATH = matcher.group(1);
    } else {
      ROOT_PATH = path;
    }
  }
}
