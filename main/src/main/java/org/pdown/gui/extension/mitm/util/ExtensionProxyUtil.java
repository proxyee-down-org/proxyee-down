package org.pdown.gui.extension.mitm.util;

import com.sun.jna.Pointer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.extension.mitm.util.WinInet.INTERNET_PER_CONN_OPTION;
import org.pdown.gui.extension.mitm.util.WinInet.INTERNET_PER_CONN_OPTION.ByReference;
import org.pdown.gui.extension.mitm.util.WinInet.INTERNET_PER_CONN_OPTION_LIST;
import org.pdown.gui.util.ExecUtil;

/**
 * 系统的代理切换工具类
 */
public class ExtensionProxyUtil {

  /**
   * 设置PAC代理
   */
  public static void enabledPACProxy(String url) throws IOException {
    if (OsUtil.isWindows()) {
      String interName = getRemoteInterface();
      INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(interName, 2);
      INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
          .toArray(list.dwOptionCount);
      // Set flags.
      pOptions[0].dwOption = WinInet.INTERNET_PER_CONN_FLAGS;
      pOptions[0].Value.dwValue = WinInet.PROXY_TYPE_AUTO_PROXY_URL;
      pOptions[0].Value.setType(int.class);

      // Set flags.
      pOptions[1].dwOption = WinInet.INTERNET_PER_CONN_AUTOCONFIG_URL;
      pOptions[1].Value.pszValue = url;
      pOptions[1].Value.setType(String.class);

      refreshOptions(list);
    } else if (OsUtil.isMac()) {
      String networkService = disabledProxy();
      ExecUtil.httpGet("http://127.0.0.1:" + DownApplication.macToolPort + "/proxy/enabledPAC"
          + "?ns=" + networkService
          + "&url=" + url);
    }
  }

  /**
   * 启用http代理
   */
  public static void enabledHTTPProxy(String host, int port) throws IOException {
    if (OsUtil.isWindows()) {
      String interName = getRemoteInterface();
      INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(interName, 2);
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

      refreshOptions(list);
    } else if (OsUtil.isMac()) {
      String networkService = disabledProxy();
      ExecUtil.httpGet("http://127.0.0.1:" + DownApplication.macToolPort + "/proxy/enabledHTTP"
          + "?ns=" + networkService
          + "&host=" + host
          + "&port=" + port);
    }
  }

  /**
   * 禁用代理
   */
  public static String disabledProxy() throws IOException {
    if (OsUtil.isWindows()) {
      String interName = getRemoteInterface();
      INTERNET_PER_CONN_OPTION_LIST list = buildOptionList(interName, 1);
      INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
          .toArray(list.dwOptionCount);
      // Set flags.
      pOptions[0].dwOption = WinInet.INTERNET_PER_CONN_FLAGS;
      pOptions[0].Value.dwValue = WinInet.PROXY_TYPE_DIRECT;
      pOptions[0].Value.setType(int.class);

      refreshOptions(list);
    } else if (OsUtil.isMac()) {
      String networkService = getRemoteInterface();
      ExecUtil.httpGet("http://127.0.0.1:" + DownApplication.macToolPort + "/proxy/disabled"
          + "?ns=" + networkService);
      return networkService;
    }
    return null;
  }

  /**
   * 获取访问外网使用的网卡
   */
  private static String getRemoteInterface() throws IOException {
    Map<String, List<String>> interfacesInfo = getInterfacesInfo();
    Socket socket = new Socket("www.baidu.com", 80);
    for (Entry<String, List<String>> entry : interfacesInfo.entrySet()) {
      if (entry.getValue().contains(socket.getLocalAddress().getHostAddress())) {
        String remoteInterface = entry.getKey();
        if (OsUtil.isWindows()) {
          String result = ExecUtil.exec("rasdial");
          if (result != null && Arrays.stream(result.split("\r\n")).anyMatch(line -> line.equals(remoteInterface))) {
            return remoteInterface;
          }
        } else if (OsUtil.isMac()) {
          String result = ExecUtil.exec("networksetup", "-listnetworkserviceorder");
          Pattern pattern = Pattern.compile("\\(Hardware\\sPort:\\s(.*),\\sDevice:\\s(.*)\\)");
          Matcher matcher = pattern.matcher(result);
          while (matcher.find()) {
            if (matcher.group(2).equalsIgnoreCase(remoteInterface)) {
              return matcher.group(1);
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * 获取本机所有网卡
   */
  public static Map<String, List<String>> getInterfacesInfo() throws SocketException {
    Map<String, List<String>> interfacesInfo = new HashMap<>();
    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();
      Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress nextElement = addresses.nextElement();
        String name = networkInterface.getDisplayName();
        List<String> ipList = interfacesInfo.get(name);
        if (ipList == null) {
          ipList = new ArrayList<>();
          interfacesInfo.put(name, ipList);
        }
        ipList.add(nextElement.getHostAddress());
      }
    }
    return interfacesInfo;
  }

  public static void main(String[] args) throws Exception {
    System.out.println(getRemoteInterface());
  }

  private static INTERNET_PER_CONN_OPTION_LIST buildOptionList(String connectionName, int size) {
    INTERNET_PER_CONN_OPTION_LIST list = new INTERNET_PER_CONN_OPTION_LIST();
    // Fill the list structure.
    list.dwSize = list.size();

    // NULL == LAN, otherwise connectoid name.
    list.pszConnection = connectionName;

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
}

