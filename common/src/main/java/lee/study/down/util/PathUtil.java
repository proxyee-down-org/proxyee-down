package lee.study.down.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtil {

  public static String ROOT_PATH;

  static {
    URL url = PathUtil.class.getResource("/");
    String path = url != null ? url.getPath() : PathUtil.class.getResource("").getPath();
    Pattern pattern = Pattern.compile("^file:(/[^!]*/)[^/]+\\.jar!/.*$");
    Matcher matcher = pattern.matcher(path);
    boolean needDecode = false;
    if (matcher.find()) {
      ROOT_PATH = matcher.group(1);
      needDecode = true;
    } else {
      ROOT_PATH = path;
      if ("1".equals(System.getProperty("exe4j"))) {  //exe4j中文路径特殊处理
        needDecode = true;
      }
    }
    if (needDecode) {
      ROOT_PATH = ROOT_PATH.replaceAll("\\+", "%2b");
      try {
        ROOT_PATH = URLDecoder.decode(ROOT_PATH, "UTF-8");
      } catch (UnsupportedEncodingException e) {
      }
    }
    if (OsUtil.isWindows() && ROOT_PATH.indexOf("/") == 0) {
      ROOT_PATH = ROOT_PATH.substring(1);
    }
    if (ROOT_PATH.lastIndexOf("/") != ROOT_PATH.length() - 1
        && ROOT_PATH.lastIndexOf("\\") != ROOT_PATH.length() - 1) {
      ROOT_PATH += "/";
    }
  }
}
