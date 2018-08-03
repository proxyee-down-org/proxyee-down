package org.pdown.gui.http.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.http.util.HttpHandlerUtil;

public class GetLocaleHandler implements HttpHandler {

  @Override
  public FullHttpResponse handle(Channel channel, FullHttpRequest request) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("locale", PDownConfigContent.getInstance().get().getLocale());
    HttpHandlerUtil.writeJson(channel, data);
    return null;
  }
}
