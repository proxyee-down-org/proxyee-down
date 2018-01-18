package lee.study.down.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpUtil {
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
   * 检测url是否匹配
   */
  public static boolean checkUrl(HttpRequest httpRequest, String regex) {
    return checkHead(httpRequest, HttpHeaderNames.HOST, regex);
  }

  /**
   * 检测Referer是否匹配
   */
  public static boolean checkReferer(HttpRequest httpRequest, String regex) {
    return checkHead(httpRequest, HttpHeaderNames.REFERER, regex);
  }

  /**
   * 检测某个http头是否匹配
   */
  public static boolean checkHead(HttpRequest httpRequest, CharSequence headName, String regex) {
    String host = httpRequest.headers().get(headName);
    if (host != null && regex != null) {
      String url;
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
