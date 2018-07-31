package org.pdown.gui.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.pdown.gui.http.handler.DefaultHttpHandler;
import org.pdown.gui.http.handler.HttpHandler;

public class EmbedHttpServer {

  private int port;
  private Map<String, HttpHandler> httpHandlerMap;

  public EmbedHttpServer(int port) {
    this.port = port;
    this.httpHandlerMap = new HashMap<>();
    this.addRouter("/", new DefaultHttpHandler());
  }

  public void start() {
    start(null);
  }

  public void start(GenericFutureListener startedListener) {
    NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
    try {
      ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast("httpCodec", new HttpServerCodec());
              ch.pipeline().addLast(new HttpObjectAggregator(4194304));
              ch.pipeline().addLast("serverHandle", new SimpleChannelInboundHandler<FullHttpRequest>() {

                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
                  URI uri = new URI(request.uri());
                  String path = uri.getPath();
                  HttpHandler httpHandler = EmbedHttpServer.this.httpHandlerMap
                      .entrySet()
                      .stream()
                      .filter(entry -> path.matches("^" + entry.getKey() + "(\\?.*)?$"))
                      .sorted(Comparator.comparingInt(e -> e.getKey().length()))
                      .map(entry -> entry.getValue())
                      .findFirst()
                      .orElse(EmbedHttpServer.this.httpHandlerMap.get("/"));
                  FullHttpResponse httpResponse = httpHandler.handle(ctx.channel(), request);
                  if (httpResponse != null) {
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                    ch.writeAndFlush(httpResponse);
                  }
                }

                @Override
                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                  ctx.channel().close();
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                  cause.printStackTrace();
                  FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
                  httpResponse.content().writeBytes(cause.getMessage().getBytes());
                  httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
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

  public void addRouter(String uri, HttpHandler httpHandler) {
    this.httpHandlerMap.put(uri, httpHandler);
  }

  public static void main(String[] args) {
    new EmbedHttpServer(8998).start();
  }
}
