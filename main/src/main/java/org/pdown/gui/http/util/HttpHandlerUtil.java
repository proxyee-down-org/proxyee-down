package org.pdown.gui.http.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import java.nio.charset.Charset;

public class HttpHandlerUtil {

  public static void writeJson(Channel channel, Object obj) {
    channel.writeAndFlush(buildJson(obj));
  }

  public static FullHttpResponse buildJson(Object obj) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, AsciiString.cached("application/json; charset=utf-8"));
    if (obj != null) {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(obj);
        response.content().writeBytes(content.getBytes(Charset.forName("utf-8")));
      } catch (JsonProcessingException e) {
        response.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
      }
    }
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
    return response;
  }

  public static FullHttpResponse buildContent(String content, String contentType) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, AsciiString.cached(contentType));
    if (content != null) {
      response.content().writeBytes(content.getBytes(Charset.forName("utf-8")));
    }
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
    return response;
  }
}
