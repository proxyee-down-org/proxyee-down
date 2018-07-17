package org.pdown.gui.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;

public class EmbedHttpServer {

  private int port;
  private String root;

  public EmbedHttpServer(int port, String root) {
    this.port = port;
    this.root = root;
  }

  public void start() {
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
              ch.pipeline().addLast("serverHandle", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  if (msg instanceof HttpRequest) {
                    FullHttpRequest request = (FullHttpRequest) msg;
                    URI uri = new URI(request.uri());
                    String path = uri.getPath();
                    if ("/".equals(path)) {
                      path = "/index.html";
                    }
                    File file = new File(root + path);
                    FullHttpResponse httpResponse;
                    if (file.exists()) {
                      String mime = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                      httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                      buildHead(httpResponse, mime, file.length());
                      httpResponse.content().writeBytes(Files.readAllBytes(file.toPath()));
                    } else {
                      httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                      buildHead(httpResponse, null, file.length());
                    }
                    ctx.channel().writeAndFlush(httpResponse);
                  }
                }

                @Override
                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                  ctx.channel().close();
                }
              });
            }
          });
      ChannelFuture f = bootstrap.bind(port)
          .sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }
  }

  private void buildHead(FullHttpResponse httpResponse, String mime, long size) {
    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, size);
    if (mime != null) {
      AsciiString contentType;
      switch (mime) {
        case "txt":
        case "text":
          contentType = HttpHeaderValues.TEXT_PLAIN;
          break;
        case "html":
        case "htm":
          contentType = AsciiString.cached("text/html;charset=utf-8");
          break;
        case "css":
          contentType = AsciiString.cached("text/css");
          break;
        case "js":
          contentType = AsciiString.cached("application/javascript");
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
        default:
          contentType = HttpHeaderValues.APPLICATION_OCTET_STREAM;
      }
      httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }
  }

  public static void main(String[] args) {
    new EmbedHttpServer(8998, "E:\\work\\smartlink\\front\\dist").start();
  }
}
