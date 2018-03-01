package lee.study.down.jna;

import com.sun.jna.Pointer;
import lee.study.down.jna.WinInet.INTERNET_PER_CONN_OPTION.ByReference;
import lee.study.down.jna.WinInet.INTERNET_PER_CONN_OPTION_LIST;

public class WinInetImpl {

  public static INTERNET_PER_CONN_OPTION_LIST buildOptionList(int size) {
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

  public static boolean refreshOptions(INTERNET_PER_CONN_OPTION_LIST list) {
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
