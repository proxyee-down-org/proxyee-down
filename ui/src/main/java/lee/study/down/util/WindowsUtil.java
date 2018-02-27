package lee.study.down.util;

import com.sun.jna.Pointer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import lee.study.down.jna.WinInet;
import lee.study.down.jna.WinInet.INTERNET_PER_CONN_OPTION;
import lee.study.down.jna.WinInet.INTERNET_PER_CONN_OPTION.ByReference;
import lee.study.down.jna.WinInet.INTERNET_PER_CONN_OPTION_LIST;
import lee.study.proxyee.crt.CertUtil;

public class WindowsUtil {

  private static INTERNET_PER_CONN_OPTION_LIST buildOptionList(int size) {
    INTERNET_PER_CONN_OPTION_LIST list = new INTERNET_PER_CONN_OPTION_LIST();

    // Fill the list structure.
    list.dwSize = list.size();

    // NULL == LAN, otherwise connectoid name.
    list.pszConnection = null;

    // Set three options.
    list.dwOptionCount = size;
    list.pOptions = new ByReference();

    // Ensure that the memory was allocated.
    if (null == list.pOptions) {
      // Return FALSE if the memory wasn't allocated.
      return null;
    }
    return list;
  }

  public static boolean enabledPACProxy(String pac) {
    INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(2);
    if (list == null) {
      return false;
    }
    INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
        .toArray(list.dwOptionCount);
    // Set flags.
    pOptions[0].dwOption = WinInet.INTERNET_PER_CONN_FLAGS;
    pOptions[0].Value.dwValue = WinInet.PROXY_TYPE_AUTO_PROXY_URL;
    pOptions[0].Value.setType(int.class);

    // Set flags.
    pOptions[1].dwOption = WinInet.INTERNET_PER_CONN_AUTOCONFIG_URL;
    pOptions[1].Value.pszValue = pac;
    pOptions[1].Value.setType(String.class);

    return refreshOptions(list);
  }

  public static boolean enabledIEProxy(String host, int port) {
    INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(2);
    if (list == null) {
      return false;
    }
    INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
        .toArray(list.dwOptionCount);

    // Set flags.
    pOptions[0].dwOption = WinInet.INTERNET_PER_CONN_FLAGS;
    pOptions[0].Value.dwValue = WinInet.PROXY_TYPE_PROXY;
    pOptions[0].Value.setType(int.class);

    // Set proxy name.
    pOptions[1].dwOption = WinInet.INTERNET_PER_CONN_PROXY_SERVER;
    pOptions[1].Value.pszValue = host + ":" + port;
    pOptions[1].Value.setType(String.class);

    return refreshOptions(list);
  }

  private static boolean refreshOptions(INTERNET_PER_CONN_OPTION_LIST list) {
    if (!WinInet.INSTANCE
        .InternetSetOption(Pointer.NULL, WinInet.INTERNET_OPTION_PER_CONNECTION_OPTION, list,
            list.size())) {
      return false;
    }

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

  public static boolean disabledProxy() {
    INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(1);
    if (list == null) {
      return false;
    }
    INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
        .toArray(list.dwOptionCount);
    // Set flags.
    pOptions[0].dwOption = WinInet.INTERNET_PER_CONN_FLAGS;
    pOptions[0].Value.dwValue = WinInet.PROXY_TYPE_DIRECT;
    pOptions[0].Value.setType(int.class);
    return refreshOptions(list);
  }


  /**
   * 证书是否已存在
   */
  public static boolean existsCert(InputStream input) throws Exception {
    return existsCert(getCertId(input));
  }

  /**
   * 证书是否已存在
   */

  public static boolean existsCert(String param) throws IOException {
    Process process = Runtime.getRuntime().exec("certutil "
        + "-store "
        + "root "
        + param
    );
    String ret = getProcessPrint(process);
    if (ret.indexOf("======") != -1) {
      return true;
    } else {
      process = Runtime.getRuntime().exec("certutil "
          + "-store "
          + "-user "
          + "root "
          + param
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
  public static void installCert(String path) throws IOException {
    getProcessPrint(Runtime.getRuntime().exec("certutil "
        + "-addstore "
        + (OsUtil.isAdmin() ? "" : "-user ")
        + "root "
        + "\"" + path + "\""
    ));
  }

  /**
   * 删除证书
   */
  public static void unistallCert(InputStream input) throws Exception {
    getProcessPrint(Runtime.getRuntime().exec("certutil "
        + "-delstore "
        + (OsUtil.isAdmin() ? "" : "-user ")
        + "root "
        + getCertId(input)
    ));
  }

  /**
   * 删除证书
   */
  public static void unistallCert(String param) throws Exception {
    getProcessPrint(Runtime.getRuntime().exec("certutil "
        + "-delstore "
        + (OsUtil.isAdmin() ? "" : "-user ")
        + "root "
        + param
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
