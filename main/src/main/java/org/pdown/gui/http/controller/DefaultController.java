package org.pdown.gui.http.controller;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import java.io.InputStream;
import java.net.URI;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
public class DefaultController {

  public FullHttpResponse handle(Channel channel, FullHttpRequest request) throws Exception {
    URI uri = new URI(request.uri());
    String path = uri.getPath();
    if ("/".equals(path)) {
      path = "/index.html";
    }
    InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("http" + path);
    FullHttpResponse httpResponse;
    if (inputStream != null) {
      String mime = path.substring(path.lastIndexOf(".") + 1);
      httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      buildHead(httpResponse, mime);
      try {
        byte[] bts = new byte[8192];
        int len;
        while ((len = inputStream.read(bts)) != -1) {
          httpResponse.content().writeBytes(bts, 0, len);
        }
      } finally {
        inputStream.close();
      }
    } else {
      httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.NOT_FOUND);
      buildHead(httpResponse, null);
    }
    return httpResponse;
  }

  private void buildHead(FullHttpResponse httpResponse, String mime) {
    if (mime != null) {
      AsciiString contentType;
      switch (mime) {
        case "txt":
        case "text":
          contentType = AsciiString.cached("text/plain; charset=utf-8");
          break;
        case "html":
        case "htm":
          contentType = AsciiString.cached("text/html; charset=utf-8");
          break;
        case "css":
          contentType = AsciiString.cached("text/css; charset=utf-8");
          break;
        case "js":
          contentType = AsciiString.cached("application/javascript; charset=utf-8");
          break;
        case "png":
          contentType = AsciiString.cached("image/png");
          break;
        case "jpg":
        case "jpeg":
          contentType = AsciiString.cached("image/jpeg");
          break;
        case "bmp":
          contentType = AsciiString.cached("application/x-bmp");
          break;
        case "gif":
          contentType = AsciiString.cached("image/gif");
          break;
        case "ico":
          contentType = AsciiString.cached("image/x-icon");
          break;
        case "ttf":
          contentType = AsciiString.cached("font/ttf; charset=utf-8");
          break;
        case "woff":
          contentType = AsciiString.cached("application/font-woff; charset=utf-8");
          break;
        default:
          contentType = HttpHeaderValues.APPLICATION_OCTET_STREAM;
      }
      httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }
  }
}
