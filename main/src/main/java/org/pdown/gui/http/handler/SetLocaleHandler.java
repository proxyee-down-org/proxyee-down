package org.pdown.gui.http.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.springframework.util.StringUtils;

public class SetLocaleHandler implements HttpHandler {

  @Override
  public FullHttpResponse handle(Channel channel, FullHttpRequest request) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(request.content().toString(Charset.defaultCharset()), Map.class);
    String locale = map.get("locale");
    if (!StringUtils.isEmpty(locale)) {
      PDownConfigContent.getInstance().get().setLocale(locale);
    }
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }
}
