package lee.study.hanndle;

import io.netty.buffer.ByteBuf;
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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import lee.study.down.HttpDownCallback;
import lee.study.model.ChunkInfo;
import lee.study.model.TaskInfo;
import lee.study.proxyee.server.HttpProxyServer;

public class HttpDownInitializer extends ChannelInitializer {

  private boolean isSsl;
  private TaskInfo taskInfo;
  private ChunkInfo chunkInfo;
  private HttpDownCallback callback;

  private FileChannel fileChannel;

  public HttpDownInitializer(boolean isSsl, TaskInfo taskInfo, ChunkInfo chunkInfo,
      HttpDownCallback callback) throws Exception {
    this.isSsl = isSsl;
    this.taskInfo = taskInfo;
    this.chunkInfo = chunkInfo;
    this.callback = callback;

    this.fileChannel = new RandomAccessFile(
        new File(taskInfo.getFilePath() + File.separator + taskInfo.getFileName()), "rw")
        .getChannel();
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
            byteBuf.readBytes(fileChannel, chunkInfo.getStartPosition() + chunkInfo.getDownSize(),
                readableBytes);
            //文件已下载大小
            chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
            taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
            callback.progress(taskInfo, chunkInfo);
            //分段下载完成关闭fileChannel
            if (httpContent instanceof LastHttpContent || chunkInfo.getDownSize() == chunkInfo
                .getTotalSize()) {
              fileChannel.close();
              //分段下载完成回调
              chunkInfo.setStatus(2);
              chunkInfo.setLastTime(System.currentTimeMillis());
              callback.chunkDone(taskInfo, chunkInfo);
              if (taskInfo.getChunkInfoList().stream()
                  .allMatch((chunk) -> chunk.getStatus() == 2)) {
                //文件下载完成回调
                taskInfo.setStatus(2);
                taskInfo.setLastTime(System.currentTimeMillis());
                callback.done(taskInfo);
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

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        taskInfo.setStatus(3);
        chunkInfo.setStatus(3);
        callback.error(taskInfo, chunkInfo, cause);
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
      }

      @Override
      public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ctx.channel().close();
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
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
    byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
    System.out.println(byteBuf.readableBytes());
  }
}
