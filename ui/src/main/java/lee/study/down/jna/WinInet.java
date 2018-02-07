package lee.study.down.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.util.Arrays;
import java.util.List;

public interface WinInet extends StdCallLibrary {

  WinInet INSTANCE = (WinInet) Native
      .loadLibrary("wininet", WinInet.class, W32APIOptions.UNICODE_OPTIONS);

  int INTERNET_OPTION_REFRESH = 37;
  int INTERNET_OPTION_PROXY_SETTINGS_CHANGED = 95;

  boolean InternetSetOption(Pointer hInternet, int dwOption, Pointer lpBuffer, int dwBufferLength);
}
