package org.pdown.gui.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pdown.gui.http.controller.DefaultController;
import org.pdown.gui.http.controller.NativeController;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

public class EmbedHttpServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbedHttpServer.class);

  private int port;
  private DefaultController defaultController;
  private List<Object> controllerList;

  public EmbedHttpServer(int port) {
    this.port = port;
    this.defaultController = new DefaultController();
    this.controllerList = new ArrayList<>();
  }

  //根据请求uri找到对应的处理类方法执行
  public FullHttpResponse invoke(String uri, Channel channel, FullHttpRequest request)
      throws Exception {
    if (controllerList != null) {
      for (Object obj : controllerList) {
        Class<?> clazz = obj.getClass();
        RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
        if (mapping != null) {
          String mappingUri = fixUri(mapping.value()[0]);
          for (Method actionMethod : clazz.getMethods()) {
            RequestMapping subMapping = actionMethod.getAnnotation(RequestMapping.class);
            if (subMapping != null) {
              String subMappingUri = fixUri(subMapping.value()[0]);
              if (uri.equalsIgnoreCase(mappingUri + subMappingUri)) {
                return (FullHttpResponse) actionMethod.invoke(obj, channel, request);
              }
            }
          }
        }
      }
    }
    return defaultController.handle(channel, request);
  }

  private String fixUri(String uri) {
    StringBuilder builder = new StringBuilder(uri);
    if (builder.indexOf("/") != 0) {
      builder.insert(0, "/");
    }
    if (builder.lastIndexOf("/") == builder.length() - 1) {
      builder.delete(builder.length() - 1, builder.length());
    }
    return builder.toString();
  }

  public void start() {
    start(null);
  }

  public void start(GenericFutureListener startedListener) {
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    NioEventLoopGroup workGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast("httpCodec", new HttpServerCodec());
              ch.pipeline().addLast(new HttpObjectAggregator(4194304));
              ch.pipeline()
                  .addLast("serverHandle", new SimpleChannelInboundHandler<FullHttpRequest>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
                        throws Exception {
                      URI uri = new URI(request.uri());
                      FullHttpResponse httpResponse = invoke(uri.getPath(), ctx.channel(), request);
                      if (httpResponse != null) {
                        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                        ch.writeAndFlush(httpResponse);
                      }
                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) {
                      ctx.channel().close();
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                      LOGGER.error("native request error", cause.getCause() == null ? cause : cause.getCause());
                      Map<String, Object> data = new HashMap<>();
                      data.put("error", cause.getCause().toString());
                      FullHttpResponse httpResponse = HttpHandlerUtil.buildJson(data);
                      httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                      ctx.channel().writeAndFlush(httpResponse);
                    }
                  });
            }
          });
      ChannelFuture f = bootstrap.bind("127.0.0.1", port).sync();
      if (startedListener != null) {
        f.addListener(startedListener);
      }
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }
  }

  public EmbedHttpServer addController(Object obj) {
    this.controllerList.add(obj);
    return this;
  }

  public static void main(String[] args) {

    new EmbedHttpServer(8998)
        .addController(new NativeController())
        .start();
  }
}
