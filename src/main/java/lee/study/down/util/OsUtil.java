package lee.study.down.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import lee.study.down.HttpDownServer;

public class OsUtil {

  private static final String REG_HEAD = "reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ";
  private static final String REG_TAIL = " /f";
  private static final String PROXY_ENABLE_KEY = "ProxyEnable ";
  private static final String PROXY_SERVER_KEY = "ProxyServer ";
  private static final String PROXY_OVERRIDE_KEY = "ProxyOverride ";
  private static final String REG_TYPE_DWORD = " /t REG_DWORD";

  /**
   * 获取空闲端口号
   */
  public static int getFreePort() {
    try {
      InetAddress address = InetAddress.getByName("0.0.0.0");
      for (int i = 9000; i <= 65536; i++) {
        try {
          DatagramSocket ds = new DatagramSocket(i);
          ds.close();
          return i;
        } catch (SocketException e) {

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  public static void enabledIEProxy(String host, int port) throws IOException {
    Runtime.getRuntime().exec(REG_HEAD + PROXY_ENABLE_KEY + "/d 1" + REG_TYPE_DWORD + REG_TAIL);
    Runtime.getRuntime().exec(REG_HEAD + PROXY_SERVER_KEY + "/d " + host + ":" + port + REG_TAIL);
    Runtime.getRuntime().exec(REG_HEAD + PROXY_OVERRIDE_KEY + "/d <local>" + REG_TAIL);
  }

  public static void disabledIEProxy() throws IOException {
    Runtime.getRuntime().exec(REG_HEAD + PROXY_ENABLE_KEY + "/d 0" + REG_TYPE_DWORD + REG_TAIL);
  }

  public static void execFile(InputStream inputStream, String dirPath) throws IOException {
    File file = new File(dirPath + File.separator + "ca.crt");
    try (
        FileOutputStream fos = new FileOutputStream(file)
    ) {
      byte[] bts = new byte[8192];
      int len;
      while ((len = inputStream.read(bts)) != -1) {
        fos.write(bts, 0, len);
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    Desktop.getDesktop().open(file);
  }

}
