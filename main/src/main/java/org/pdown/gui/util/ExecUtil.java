package org.pdown.gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import org.pdown.core.util.OsUtil;

public class ExecUtil {

  /**
   * 执行shell并返回标准输出文本内容
   */
  public static String exec(String... shell) throws IOException {
    Process process = Runtime.getRuntime().exec(shell);
    StringBuilder sb = new StringBuilder();
    Charset charset = OsUtil.isWindows() ? Charset.forName("GBK") : Charset.defaultCharset();
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))
    ) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line + System.lineSeparator());
      }
    } finally {
      process.destroy();
    }
    return sb.toString();
  }

  /**
   * 同步执行shell，阻塞当前线程
   */
  public static void execBlock(String... shell) throws IOException {
    Process process = Runtime.getRuntime().exec(shell);
    try (
        InputStream inputStream = process.getInputStream()
    ) {
      byte[] bytes = new byte[8192];
      while ((inputStream.read(bytes)) != -1) {
        //Do nothing
      }
    } finally {
      process.destroy();
    }
  }

  /**
   * 以管理员权限，同步执行shell，阻塞当前线程
   */
  public static void execBlockWithAdmin(String shell) throws IOException {
    //osascript -e "do shell script \"shell\" with administrator privileges"
    Process process = Runtime.getRuntime().exec(new String[]{
        "osascript",
        "-e",
        "do shell script \"" +
            shell +
            "\"" +
            "with administrator privileges"
    });
    try (
        InputStream inputStream = process.getInputStream()
    ) {
      byte[] bytes = new byte[8192];
      while ((inputStream.read(bytes)) != -1) {
        //Do nothing
      }
    } finally {
      process.destroy();
    }
  }

  public static void httpGet(String url) throws IOException {
    URL u = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
    if (connection.getResponseCode() != 200) {
      throw new RuntimeException("http get error:" + url);
    }
  }


  public static void main(String[] args) throws Exception {
    httpGet("http://www.baidu.com");
  }
}
