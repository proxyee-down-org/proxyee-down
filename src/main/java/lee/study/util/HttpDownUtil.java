package lee.study.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import java.util.Map.Entry;

public class HttpDownUtil {

  /**
   * 检测请求头是否存在
   */
  public static boolean checkHeadKey(HttpHeaders httpHeaders, String regex) {
    for (Entry<String, String> entry : httpHeaders) {
      if (entry.getKey().matches(regex)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检测是url是否匹配
   */
  public static boolean checkUrl(HttpRequest httpRequest, String regex) {
    String host = httpRequest.headers().get(HttpHeaderNames.HOST);
    if (host != null && regex != null) {
      String url = null;
      if (httpRequest.uri().indexOf("/") == 0) {
        url = host + httpRequest.uri();
      } else {
        url = httpRequest.uri();
      }
      return url.matches(regex);
    }
    return false;
  }
}
