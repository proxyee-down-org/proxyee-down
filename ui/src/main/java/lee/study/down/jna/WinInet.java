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
  WinInet INSTANCE = (WinInet) Native.loadLibrary("wininet", WinInet.class, W32APIOptions.UNICODE_OPTIONS);

  int INTERNET_PER_CONN_FLAGS                         = 1;
  int INTERNET_PER_CONN_PROXY_SERVER                  = 2;
  int INTERNET_PER_CONN_PROXY_BYPASS                  = 3;
  int INTERNET_PER_CONN_AUTOCONFIG_URL                = 4;
  int INTERNET_PER_CONN_AUTODISCOVERY_FLAGS           = 5;
  int INTERNET_PER_CONN_AUTOCONFIG_SECONDARY_URL      = 6;
  int INTERNET_PER_CONN_AUTOCONFIG_RELOAD_DELAY_MINS  = 7;
  int INTERNET_PER_CONN_AUTOCONFIG_LAST_DETECT_TIME   = 8;
  int INTERNET_PER_CONN_AUTOCONFIG_LAST_DETECT_URL    = 9;

  int PROXY_TYPE_DIRECT                               = 0x00000001;   // direct to net
  int PROXY_TYPE_PROXY                                = 0x00000002;   // via named proxy
  int PROXY_TYPE_AUTO_PROXY_URL                       = 0x00000004;   // autoproxy URL
  int PROXY_TYPE_AUTO_DETECT                          = 0x00000008;   // use autoproxy detection

  //
  // options manifests for Internet{Query|Set}Option
  //

  int INTERNET_OPTION_CALLBACK                = 1;
  int INTERNET_OPTION_CONNECT_TIMEOUT         = 2;
  int INTERNET_OPTION_CONNECT_RETRIES         = 3;
  int INTERNET_OPTION_CONNECT_BACKOFF         = 4;
  int INTERNET_OPTION_SEND_TIMEOUT            = 5;
  int INTERNET_OPTION_CONTROL_SEND_TIMEOUT    = INTERNET_OPTION_SEND_TIMEOUT;
  int INTERNET_OPTION_RECEIVE_TIMEOUT         = 6;
  int INTERNET_OPTION_CONTROL_RECEIVE_TIMEOUT = INTERNET_OPTION_RECEIVE_TIMEOUT;
  int INTERNET_OPTION_DATA_SEND_TIMEOUT       = 7;
  int INTERNET_OPTION_DATA_RECEIVE_TIMEOUT    = 8;
  int INTERNET_OPTION_HANDLE_TYPE             = 9;
  int INTERNET_OPTION_LISTEN_TIMEOUT          = 11;
  int INTERNET_OPTION_READ_BUFFER_SIZE        = 12;
  int INTERNET_OPTION_WRITE_BUFFER_SIZE       = 13;

  int INTERNET_OPTION_ASYNC_ID                = 15;
  int INTERNET_OPTION_ASYNC_PRIORITY          = 16;

  int INTERNET_OPTION_PARENT_HANDLE           = 21;
  int INTERNET_OPTION_KEEP_CONNECTION         = 22;
  int INTERNET_OPTION_REQUEST_FLAGS           = 23;
  int INTERNET_OPTION_EXTENDED_ERROR          = 24;

  int INTERNET_OPTION_OFFLINE_MODE            = 26;
  int INTERNET_OPTION_CACHE_STREAM_HANDLE     = 27;
  int INTERNET_OPTION_USERNAME                = 28;
  int INTERNET_OPTION_PASSWORD                = 29;
  int INTERNET_OPTION_ASYNC                   = 30;
  int INTERNET_OPTION_SECURITY_FLAGS          = 31;
  int INTERNET_OPTION_SECURITY_CERTIFICATE_STRUCT = 32;
  int INTERNET_OPTION_DATAFILE_NAME           = 33;
  int INTERNET_OPTION_URL                     = 34;
  int INTERNET_OPTION_SECURITY_CERTIFICATE    = 35;
  int INTERNET_OPTION_SECURITY_KEY_BITNESS    = 36;
  int INTERNET_OPTION_REFRESH                 = 37;
  int INTERNET_OPTION_PROXY                   = 38;
  int INTERNET_OPTION_SETTINGS_CHANGED        = 39;
  int INTERNET_OPTION_VERSION                 = 40;
  int INTERNET_OPTION_USER_AGENT              = 41;
  int INTERNET_OPTION_END_BROWSER_SESSION     = 42;
  int INTERNET_OPTION_PROXY_USERNAME          = 43;
  int INTERNET_OPTION_PROXY_PASSWORD          = 44;
  int INTERNET_OPTION_CONTEXT_VALUE           = 45;
  int INTERNET_OPTION_CONNECT_LIMIT           = 46;
  int INTERNET_OPTION_SECURITY_SELECT_CLIENT_CERT = 47;
  int INTERNET_OPTION_POLICY                  = 48;
  int INTERNET_OPTION_DISCONNECTED_TIMEOUT    = 49;
  int INTERNET_OPTION_CONNECTED_STATE         = 50;
  int INTERNET_OPTION_IDLE_STATE              = 51;
  int INTERNET_OPTION_OFFLINE_SEMANTICS       = 52;
  int INTERNET_OPTION_SECONDARY_CACHE_KEY     = 53;
  int INTERNET_OPTION_CALLBACK_FILTER         = 54;
  int INTERNET_OPTION_CONNECT_TIME            = 55;
  int INTERNET_OPTION_SEND_THROUGHPUT         = 56;
  int INTERNET_OPTION_RECEIVE_THROUGHPUT      = 57;
  int INTERNET_OPTION_REQUEST_PRIORITY        = 58;
  int INTERNET_OPTION_HTTP_VERSION            = 59;
  int INTERNET_OPTION_RESET_URLCACHE_SESSION  = 60;
  int INTERNET_OPTION_ERROR_MASK              = 62;
  int INTERNET_OPTION_FROM_CACHE_TIMEOUT      = 63;
  int INTERNET_OPTION_BYPASS_EDITED_ENTRY     = 64;
  int INTERNET_OPTION_DIAGNOSTIC_SOCKET_INFO  = 67;
  int INTERNET_OPTION_CODEPAGE                = 68;
  int INTERNET_OPTION_CACHE_TIMESTAMPS        = 69;
  int INTERNET_OPTION_DISABLE_AUTODIAL        = 70;
  int INTERNET_OPTION_MAX_CONNS_PER_SERVER     = 73;
  int INTERNET_OPTION_MAX_CONNS_PER_1_0_SERVER = 74;
  int INTERNET_OPTION_PER_CONNECTION_OPTION   = 75;
  int INTERNET_OPTION_DIGEST_AUTH_UNLOAD             = 76;
  int INTERNET_OPTION_IGNORE_OFFLINE           = 77;
  int INTERNET_OPTION_IDENTITY                 = 78;
  int INTERNET_OPTION_REMOVE_IDENTITY          = 79;
  int INTERNET_OPTION_ALTER_IDENTITY           = 80;
  int INTERNET_OPTION_SUPPRESS_BEHAVIOR        = 81;
  int INTERNET_OPTION_AUTODIAL_MODE            = 82;
  int INTERNET_OPTION_AUTODIAL_CONNECTION      = 83;
  int INTERNET_OPTION_CLIENT_CERT_CONTEXT      = 84;
  int INTERNET_OPTION_AUTH_FLAGS               = 85;
  int INTERNET_OPTION_COOKIES_3RD_PARTY        = 86;
  int INTERNET_OPTION_DISABLE_PASSPORT_AUTH    = 87;
  int INTERNET_OPTION_SEND_UTF8_SERVERNAME_TO_PROXY         = 88;
  int INTERNET_OPTION_EXEMPT_CONNECTION_LIMIT  = 89;
  int INTERNET_OPTION_ENABLE_PASSPORT_AUTH     = 90;

  int INTERNET_OPTION_HIBERNATE_INACTIVE_WORKER_THREADS       = 91;
  int INTERNET_OPTION_ACTIVATE_WORKER_THREADS                 = 92;
  int INTERNET_OPTION_RESTORE_WORKER_THREAD_DEFAULTS          = 93;
  int INTERNET_OPTION_SOCKET_SEND_BUFFER_LENGTH               = 94;
  int INTERNET_OPTION_PROXY_SETTINGS_CHANGED                  = 95;

  int INTERNET_OPTION_DATAFILE_EXT                                              = 96;


  // BOOL InternetSetOption(
  //   _In_  HINTERNET hInternet,
  //   _In_  DWORD dwOption,
  //   _In_  LPVOID lpBuffer,
  //   _In_  DWORD dwBufferLength
  // );
  boolean InternetSetOption(Pointer hInternet, int dwOption, INTERNET_PER_CONN_OPTION_LIST lpBuffer,
      int dwBufferLength);
  boolean InternetSetOption(Pointer hInternet, int dwOption, Pointer lpBuffer, int dwBufferLength);

  // typedef struct _FILETIME {
  //   DWORD dwLowDateTime;
  //   DWORD dwHighDateTime;
  // } FILETIME, *PFILETIME;
  class FILETIME extends Structure {
    public static class ByReference extends FILETIME implements Structure.ByReference {
      public ByReference() {
      }

      public ByReference(Pointer memory) {
        super(memory);
      }
    }

    public FILETIME() {
    }

    public FILETIME(Pointer memory) {
      super(memory);
      read();
    }

    public int dwLowDateTime;
    public int dwHighDateTime;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
      return Arrays.asList("dwLowDateTime", "dwHighDateTime");
    }
  }

  // typedef struct {
  //   DWORD dwOption;
  //   union {
  //     DWORD    dwValue;
  //     LPTSTR   pszValue;
  //     FILETIME ftValue;
  //   } Value;
  // } INTERNET_PER_CONN_OPTION, *LPINTERNET_PER_CONN_OPTION;
  class INTERNET_PER_CONN_OPTION extends Structure {
    public static class ByReference extends INTERNET_PER_CONN_OPTION implements Structure.ByReference {
      public ByReference() {
      }

      public ByReference(Pointer memory) {
        super(memory);
      }
    }

    public INTERNET_PER_CONN_OPTION() {
    }

    public INTERNET_PER_CONN_OPTION(Pointer memory) {
      super(memory);
      read();
    }

    public static class VALUE_UNION extends Union {
      public int dwValue;
      public String pszValue;
      public FILETIME ftValue;
    }

    public int dwOption;
    public VALUE_UNION Value = new VALUE_UNION();

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
      return Arrays.asList("dwOption", "Value");
    }
  }

  // typedef struct {
  //   DWORD                      dwSize;
  //   LPTSTR                     pszConnection;
  //   DWORD                      dwOptionCount;
  //   DWORD                      dwOptionError;
  //   LPINTERNET_PER_CONN_OPTION pOptions;
  // } INTERNET_PER_CONN_OPTION_LIST, *LPINTERNET_PER_CONN_OPTION_LIST;
  class INTERNET_PER_CONN_OPTION_LIST extends Structure {
    public static class ByReference extends INTERNET_PER_CONN_OPTION_LIST implements Structure.ByReference {
      public ByReference() {
      }

      public ByReference(Pointer memory) {
        super(memory);
      }
    }

    public INTERNET_PER_CONN_OPTION_LIST() {
    }

    public INTERNET_PER_CONN_OPTION_LIST(Pointer memory) {
      super(memory);
      read();
    }

    public int dwSize;
    public String pszConnection;
    public int dwOptionCount;
    public int dwOptionError;
    public INTERNET_PER_CONN_OPTION.ByReference pOptions;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
      return Arrays
          .asList("dwSize", "pszConnection", "dwOptionCount", "dwOptionError", "pOptions");
    }
  }
}
