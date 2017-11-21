package lee.study;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TestServer {

  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
//                    .option(ChannelOption.SO_BACKLOG, 100)
          .option(ChannelOption.TCP_NODELAY, true)
//                    .handler(new LoggingHandler(LogLevel.ERROR))
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                  super.channelRegistered(ctx);
                  System.out.println("channelRegistered");
                }

                @Override
                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                  super.channelUnregistered(ctx);
                  System.out.println("channelUnregistered");
                }

                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                  super.channelActive(ctx);
                  System.out.println("channelActive");
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                  super.channelInactive(ctx);
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  super.channelRead(ctx, msg);
                  System.out.println("channelRead");
                }

                @Override
                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                  super.channelReadComplete(ctx);
                  System.out.println("channelReadComplete");
                }

                @Override
                public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                    throws Exception {
                  super.userEventTriggered(ctx, evt);
                  System.out.println("userEventTriggered");
                }

                @Override
                public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                  super.channelWritabilityChanged(ctx);
                  System.out.println("channelWritabilityChanged");
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                    throws Exception {
                  System.out.println(11111);
                  //super.exceptionCaught(ctx, cause);
                  System.out.println("exceptionCaught");
                  cause.printStackTrace();
                }
              });
            }
          });
      ChannelFuture f = b
          .bind(9898)
          .sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
