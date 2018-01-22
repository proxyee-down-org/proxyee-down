package lee.study.down.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public static int getFreePort(int defaultPort) throws Exception {
    int port;
    ServerSocket serverSocket1 = null;
    try {
      serverSocket1 = new ServerSocket(defaultPort);
      port = serverSocket1.getLocalPort();
    } catch (Exception e) {
      ServerSocket serverSocket2 = null;
      try {
        serverSocket2 = new ServerSocket(0);
        port = serverSocket2.getLocalPort();
      } catch (IOException e1) {
        throw e1;
      } finally {
        if (serverSocket2 != null) {
          serverSocket2.close();
        }
      }
    } finally {
      if (serverSocket1 != null) {
        serverSocket1.close();
      }
    }
    return port;
  }

  /**
   * 检查端口号是否被占用
   */
  public static boolean isBusyPort(int port) {
    boolean ret = true;
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      ret = false;
    } catch (Exception e) {
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return ret;
  }

  public static void openBrowse(String url) throws Exception {
    Desktop desktop = Desktop.getDesktop();
    boolean flag = Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE);
    if (flag) {
      try {
        URI uri = new URI(url);
        desktop.browse(uri);
      } catch (Exception e) {
        throw new Exception("can't open browse", e);
      }
    } else {
      throw new Exception("don't support browse");
    }
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

  private static final String OS = System.getProperty("os.name").toLowerCase();

  public static boolean isWindows() {
    return OS.indexOf("win") >= 0;
  }

  public static boolean isMac() {
    return OS.indexOf("mac") >= 0;
  }

  public static boolean isUnix() {
    return OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") >= 0;
  }

  private static final String ARCH = System.getProperty("sun.arch.data.model");

  public static boolean is64() {
    return "64".equals(ARCH);
  }

  public static boolean is32() {
    return "32".equals(ARCH);
  }

  public static void main(String[] args) {
    System.out.println(System.getProperty("sun.arch.data.model"));
  }

  public static boolean isSolaris() {
    return (OS.indexOf("sunos") >= 0);
  }

}
