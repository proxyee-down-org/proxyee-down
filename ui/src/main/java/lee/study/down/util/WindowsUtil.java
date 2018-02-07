package lee.study.down.util;

import com.sun.jna.Pointer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import lee.study.down.jna.WinInet;
import lee.study.proxyee.crt.CertUtil;

public class WindowsUtil {

  private static final String HEAD_COMMON = " \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ";
  private static final String REG_ADD_HEAD = "reg add" + HEAD_COMMON;
  private static final String REG_DEL_HEAD = "reg delete" + HEAD_COMMON;
  private static final String REG_TAIL = " /f";
  private static final String PAC_URL_KEY = "AutoConfigURL ";
  private static final String PROXY_ENABLE_KEY = "ProxyEnable ";
  private static final String PROXY_SERVER_KEY = "ProxyServer ";
  private static final String PROXY_OVERRIDE_KEY = "ProxyOverride ";
  private static final String REG_TYPE_DWORD = " /t REG_DWORD";

  public static void enabledIEProxy(String host, int port, boolean refresh) throws IOException {
    Runtime.getRuntime().exec(REG_ADD_HEAD + PROXY_ENABLE_KEY + "/d 1" + REG_TYPE_DWORD + REG_TAIL);
    Runtime.getRuntime()
        .exec(REG_ADD_HEAD + PROXY_SERVER_KEY + "/d " + host + ":" + port + REG_TAIL);
    Runtime.getRuntime().exec(REG_ADD_HEAD + PROXY_OVERRIDE_KEY + "/d <local>" + REG_TAIL);
    if (refresh) {
      refreshOptions();
    }
  }

  public static void enabledIEProxy(String host, int port) throws IOException {
    disabledPACProxy(false);
    enabledIEProxy(host, port, true);
  }

  public static void disabledIEProxy(boolean refresh) throws IOException {
    Runtime.getRuntime().exec(REG_ADD_HEAD + PROXY_ENABLE_KEY + "/d 0" + REG_TYPE_DWORD + REG_TAIL);
    if (refresh) {
      refreshOptions();
    }
  }

  public static void enabledPACProxy(String url, boolean refresh) throws IOException {
    Runtime.getRuntime().exec(REG_ADD_HEAD + PAC_URL_KEY + "/d " + url + REG_TAIL);
    if (refresh) {
      refreshOptions();
    }
  }

  public static void enabledPACProxy(String url) throws IOException {
    disabledIEProxy(false);
    enabledPACProxy(url, true);
  }

  public static void disabledPACProxy(boolean refresh) throws IOException {
    Runtime.getRuntime().exec(REG_DEL_HEAD + PAC_URL_KEY + REG_TAIL);
    if (refresh) {
      refreshOptions();
    }
  }

  public static boolean disabledProxy() throws IOException {
    disabledPACProxy(false);
    disabledIEProxy(false);
    return refreshOptions();
  }

  private static boolean refreshOptions() {
    if (!WinInet.INSTANCE
        .InternetSetOption(Pointer.NULL, WinInet.INTERNET_OPTION_PROXY_SETTINGS_CHANGED,
            Pointer.NULL, 0)) {
      return false;
    }

    // Refresh Internet Options
    if (!WinInet.INSTANCE
        .InternetSetOption(Pointer.NULL, WinInet.INTERNET_OPTION_REFRESH, Pointer.NULL, 0)) {
      return false;
    }
    return true;
  }


  /**
   * 证书是否已存在
   */
  public static boolean existsCert(InputStream input) throws Exception {
    String certId = getCertId(input);
    Process process = Runtime.getRuntime().exec("certutil "
        + "-store "
        + "root "
        + certId
    );
    String ret = getProcessPrint(process);
    if (ret.indexOf("======") != -1) {
      return true;
    } else {
      process = Runtime.getRuntime().exec("certutil "
          + "-store "
          + "-user "
          + "root "
          + certId
      );
      ret = getProcessPrint(process);
      if (ret.indexOf("======") != -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * 安装证书至受信任的机构根目录
   */
  public static void installCert(InputStream input) throws IOException {
    String caPath = PathUtil.ROOT_PATH + "/ca.crt";
    FileUtil.initFile(caPath, input, false);
    getProcessPrint(Runtime.getRuntime().exec("certutil "
        + "-addstore "
        + (OsUtil.isAdmin() ? "" : "-user ")
        + "root "
        + "\"" + caPath + "\""
    ));
    FileUtil.deleteIfExists(caPath);
  }

  /**
   * 删除证书
   */
  public static void unistallCert(InputStream input) throws Exception {
    String certId = getCertId(input);
    getProcessPrint(Runtime.getRuntime().exec("certutil "
        + "-delstore "
        + (OsUtil.isAdmin() ? "" : "-user ")
        + "root "
        + certId
    ));
  }

  private static String getCertId(InputStream input) throws Exception {
    X509Certificate certificate = CertUtil.loadCert(input);
    return Long.toHexString(certificate.getSerialNumber().longValue());
  }

  private static String getProcessPrint(Process process) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    ) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }
    return sb.toString();
  }
}
