package org.pdown.gui.http.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import org.pdown.gui.com.Components;
import org.pdown.gui.http.util.HttpHandlerUtil;

public class FileChooserHandler implements HttpHandler {

  @Override
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
}
