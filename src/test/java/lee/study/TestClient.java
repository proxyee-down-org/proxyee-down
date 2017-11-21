package lee.study;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TestClient {

  public static void main(String[] args) throws IOException {
    Socket socket = new Socket("127.0.0.1", 9898);
    OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    outputStream.write(1);
    outputStream.flush();
    outputStream.write(1);
    outputStream.write(1);
    outputStream.flush();

        /*Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup()) // 注册线程池
                .channel(MyNioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer(){
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        super.exceptionCaught(ctx, cause);
                    }

                    @Override
                    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                        super.handlerAdded(ctx);
                    }

                    @Override
                    protected void initChannel(Channel ch) throws Exception {

                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                        System.out.println(11111);
                        super.channelUnregistered(ctx);
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        System.out.println(2222);
                        super.channelActive(ctx);
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        System.out.println(3333);
                        super.channelInactive(ctx);
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(4444);
                        super.channelRead(ctx, msg);
                    }

                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                        System.out.println(5555);
                        super.channelReadComplete(ctx);
                    }

                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                        System.out.println(6666);
                        super.userEventTriggered(ctx, evt);
                    }

                    @Override
                    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                        System.out.println(7777);
                        super.channelWritabilityChanged(ctx);
                    }
                });
        try {
            ChannelFuture cf = bootstrap.connect("127.0.0.1", 9898).sync();
            cf.channel().writeAndFlush(Unpooled.wrappedBuffer(new byte[]{1,2,3}));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
  }
}
