package lee.study.down.hanndle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import lee.study.down.HttpDownServer;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;

public class HttpDownInitializer extends ChannelInitializer {

  private boolean isSsl;
  private TaskInfo taskInfo;
  private ChunkInfo chunkInfo;
  private HttpDownCallback callback;

  private FileChannel fileChannel;
  private long realContentSize;

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
      ch.pipeline().addLast(HttpDownServer.CLIENT_SSL_CONTEXT.newHandler(ch.alloc()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
          if (msg instanceof HttpContent) {
            if (fileChannel == null || !fileChannel.isOpen()) {
              return;
            }
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf byteBuf = httpContent.content();
            int readableBytes = byteBuf.readableBytes();
            synchronized (chunkInfo) {
              if (chunkInfo.getStatus() == 1) {
                fileChannel.write(byteBuf.nioBuffer());
                //文件已下载大小
                chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
              } else {
                return;
              }
            }
            synchronized (taskInfo) {
              taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
            }
            callback.onProgress(taskInfo, chunkInfo);
            //分段下载完成关闭fileChannel
            if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
              HttpDownUtil.safeClose(ctx.channel(), fileChannel);
              //分段下载完成回调
              chunkInfo.setStatus(2);
              chunkInfo.setLastTime(System.currentTimeMillis());
              callback.onChunkDone(taskInfo, chunkInfo);
              synchronized (taskInfo) {
                if (taskInfo.getStatus() == 1 && taskInfo.getChunkInfoList().stream()
                    .allMatch((chunk) -> chunk.getStatus() == 2)) {
                  //记录完成时间
                  taskInfo.setLastTime(System.currentTimeMillis());
                  if (taskInfo.getTotalSize() <= 0) {  //chunked编码最后更新文件大小
                    taskInfo.setTotalSize(taskInfo.getDownSize());
                    taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
                  }
                  //文件下载完成回调
                  taskInfo.setStatus(2);
                  callback.onDone(taskInfo);
                }
              }
            } else if (realContentSize
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition() || (realContentSize - 1)
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition()) {  //百度响应做了手脚，会少一个字节
              //真实响应字节小于要下载的字节，在下载完成后要继续下载
              HttpDownServer.LOGGER.debug(
                  "继续下载：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
              HttpDownUtil.continueDown(taskInfo, chunkInfo);
            } else if (chunkInfo.getDownSize() > chunkInfo.getTotalSize()) {
              //错误下载从0开始重新下过
              HttpDownServer.LOGGER.error("Out of chunk size：" + chunkInfo + "\t" + taskInfo);
              synchronized (taskInfo) {
                synchronized (chunkInfo) {
                  taskInfo.setTotalSize(taskInfo.getTotalSize() - chunkInfo.getDownSize());
                }
              }
              HttpDownUtil.retryDown(taskInfo, chunkInfo, 0);
            }
          } else {
            HttpResponse httpResponse = (HttpResponse) msg;
            realContentSize = HttpDownUtil.getDownFileSize(httpResponse.headers());
            HttpDownServer.LOGGER.debug(
                "下载响应：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize() + "\t"
                    + httpResponse.headers().get(
                    HttpHeaderNames.CONTENT_RANGE) + "\t" + realContentSize);
            fileChannel = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw").getChannel();
            fileChannel.position(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
            chunkInfo.setStatus(1);
            chunkInfo.setFileChannel(fileChannel);
            callback.onChunkStart(taskInfo, chunkInfo);
          }
        } catch (Exception e) {
          throw e;
        } finally {
          ReferenceCountUtil.release(msg);
        }
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        callback.onError(taskInfo, chunkInfo, cause);
        if (cause instanceof IOException) {
          HttpDownServer.LOGGER.debug(
              "服务器响应异常重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
        } else {
          HttpDownServer.LOGGER.error("down onError:", cause);
        }
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
    RandomAccessFile r1 = new RandomAccessFile("f:/down/test1.txt", "rw");
    RandomAccessFile r2 = new RandomAccessFile("f:/down/test2.txt", "rw");
    r1.setLength(1024 * 1024 * 16);
    r1.write(new byte[]{1, 3, 6, 3, 1, 6, 5, 9, 6, 5, 7});
//    r1.write(new byte[]{2,6,3,3,9,7,4,7,8});
    r2.setLength(1024 * 1024 * 16);
//    r2.write(new byte[]{1,3,6,3,1,6,5,9,6,5,7});
    r2.write(new byte[]{2, 6, 3, 3, 9, 7, 4, 7, 8});
  }

}
