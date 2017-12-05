package lee.study.util;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map.Entry;
import lee.study.HttpDownServer;
import lee.study.model.HttpDownInfo;
import lee.study.model.HttpRequestInfo;
import lee.study.model.TaskInfo;

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

  public static void startDownTask(TaskInfo taskInfo, HttpRequest httpRequest,
      HttpResponse httpResponse, Channel clientChannel) {
    HttpHeaders httpHeaders = httpResponse.headers();
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo,
        HttpRequestInfo.adapter(httpRequest));
    HttpDownServer.DOWN_CONTENT.put(taskInfo.getId(), httpDownInfo);
    httpHeaders.clear();
    httpResponse.setStatus(HttpResponseStatus.OK);
    httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    String js =
        "<script>window.top.location.href='http://localhost:"+HttpDownServer.VIEW_SERVER_PORT+"/#/newTask/" + httpDownInfo
            .getTaskInfo().getId()
            + "';</script>";
    HttpContent content = new DefaultLastHttpContent();
    content.content().writeBytes(js.getBytes());
    httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, js.getBytes().length);
    clientChannel.writeAndFlush(httpResponse);
    clientChannel.writeAndFlush(content);
    clientChannel.close();
  }

  public static void serialize(Serializable object, String path) throws IOException {
    try (
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))
    ) {
      outputStream.writeObject(object);
    }
  }

  public static Object deserialize(String path) throws IOException, ClassNotFoundException {
    try (
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))
    ) {
      return ois.readObject();
    }
  }
}
