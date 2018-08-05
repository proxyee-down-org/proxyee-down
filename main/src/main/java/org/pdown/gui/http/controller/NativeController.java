package org.pdown.gui.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import org.pdown.gui.com.Components;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("native")
public class NativeController {

  @RequestMapping("dirChooser")
  public FullHttpResponse dirChooser(Channel channel, FullHttpRequest request) throws Exception {
    Platform.runLater(() -> {
      File file = Components.dirChooser();
      Map<String, Object> data = null;
      if (file != null) {
        data = new HashMap<>();
        data.put("path", file.getPath());
        data.put("canWrite", file.canWrite());
        data.put("freeSpace", file.getFreeSpace());
        data.put("totalSpace", file.getTotalSpace());
      }
      HttpHandlerUtil.writeJson(channel, data);
    });
    return null;
  }

  @RequestMapping("fileChooser")
  public FullHttpResponse handle(Channel channel, FullHttpRequest request) throws Exception {
    Platform.runLater(() -> {
      File file = Components.fileChooser();
      Map<String, Object> data = null;
      if (file != null) {
        data = new HashMap<>();
        data.put("name", file.getName());
        data.put("path", file.getPath());
        data.put("parent", file.getParent());
        data.put("size", file.length());
      }
      HttpHandlerUtil.writeJson(channel, data);
    });
    return null;
  }

  @RequestMapping("getLocale")
  public FullHttpResponse getLocale(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("locale", PDownConfigContent.getInstance().get().getLocale());
    HttpHandlerUtil.writeJson(channel, data);
    return null;
  }

  @RequestMapping("setLocale")
  public FullHttpResponse setLocale(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(request.content().toString(Charset.defaultCharset()), Map.class);
    String locale = map.get("locale");
    if (!StringUtils.isEmpty(locale)) {
      PDownConfigContent.getInstance().get().setLocale(locale);
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }
}
