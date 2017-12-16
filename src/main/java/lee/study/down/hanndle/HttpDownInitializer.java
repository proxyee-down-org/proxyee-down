package lee.study.down.hanndle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
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
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    this.chunkInfo.setChannel(ch);
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
            fileChannel.write(byteBuf.nioBuffer());
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
          } else {
            fileChannel = new RandomAccessFile(
                taskInfo.getFilePath() + File.separator + taskInfo.getFileName(), "rw")
                .getChannel();
            if(taskInfo.isSupportRange()){
              fileChannel.position(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
            }
            chunkInfo.setFileChannel(fileChannel);
            callback.chunkStart(taskInfo, chunkInfo);
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          ReferenceCountUtil.release(msg);
        }
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(
            "服务器响应异常重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
        chunkInfo.setStatus(3);
        callback.error(taskInfo, chunkInfo, cause);
        //super.exceptionCaught(ctx, cause);
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

  public static void main(String[] args) throws IOException {
    byte[] bts1 = new byte[26];
    for (byte i = 0, j = 'a'; i < bts1.length; i++, j++) {
      bts1[i] = j;
    }
    byte[] bts2 = new byte[26];
    for (byte i = 0, j = 'A'; i < bts2.length; i++, j++) {
      bts2[i] = j;
    }
    Files.write(Paths.get("f:/down/test1.txt"), bts1);
    Files.write(Paths.get("f:/down/test2.txt"), bts2);
  }

}
