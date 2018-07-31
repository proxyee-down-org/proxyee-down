package org.pdown.gui.http.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface HttpHandler {

  FullHttpResponse handle(Channel channel,FullHttpRequest request) throws Exception;
}
