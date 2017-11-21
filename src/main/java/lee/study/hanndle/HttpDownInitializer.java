package lee.study.hanndle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lee.study.down.HttpDownCallback;
import lee.study.proxyee.server.HttpProxyServer;

public class HttpDownInitializer extends ChannelInitializer {

  private boolean isSsl;
  private int index;
  private File file;
  private AtomicInteger doneConnections;
  private AtomicLong fileDownSize;
  private HttpDownCallback callback;

  private FileChannel fileChannel;
  private long downSize = 0;
  private int connections;

  public HttpDownInitializer(boolean isSsl, int index, File file,
      AtomicInteger doneConnections, AtomicLong fileDownSize,
      HttpDownCallback callback) throws Exception {
    this.isSsl = isSsl;
    this.index = index;
    this.file = file;
    this.doneConnections = doneConnections;
    this.fileDownSize = fileDownSize;
    this.callback = callback;

    fileChannel = new RandomAccessFile(file, "rw").getChannel();
    connections = doneConnections.get();
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    if (isSsl) {
      ch.pipeline().addLast(HttpProxyServer.clientSslCtx.newHandler(ch.alloc()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
          if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf byteBuf = httpContent.content();
            int readableBytes = byteBuf.readableBytes();
            long fileSize = file.length();
            long chunk = fileSize / connections;
            long start = index * chunk;
            long end = index + 1 == connections ? (index + 1) * chunk + fileSize % connections - 1
                : (index + 1) * chunk - 1;
            byteBuf.readBytes(fileChannel, start + downSize, readableBytes);
            downSize += readableBytes;
            callback
                .progress(index, downSize, end - start + 1, fileDownSize.addAndGet(readableBytes),
                    fileSize);
            //分段下载完成关闭
            if (httpContent instanceof LastHttpContent) {
              fileChannel.close();
              if (doneConnections.decrementAndGet() == 0) {
                //文件下载完成回调
                callback.done(index);
                ctx.channel().close();
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          ReferenceCountUtil.release(msg);
        }
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    ctx.channel().close();
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
    ctx.channel().close();
  }

  public static void main(String[] args) throws Exception {
    int connections = 2;
    long fileSize = 76351;
    long chunk = fileSize / connections;
    for (int index = 0; index < 2; index++) {
      long start = index * chunk;
      long end = index + 1 == connections ? (index + 1) * chunk + fileSize % connections - 1
          : (index + 1) * chunk - 1;
      System.out.println(start + "\t" + end);
    }
    ByteBuf byteBuf = Unpooled.buffer(5);
    byteBuf.writeBytes(new byte[]{1,2,3,4});
    System.out.println(byteBuf.readableBytes());
  }
}
