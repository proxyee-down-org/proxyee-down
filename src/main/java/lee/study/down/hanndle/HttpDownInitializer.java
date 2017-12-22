package lee.study.down.hanndle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
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
            fileChannel.write(byteBuf.nioBuffer());
            //文件已下载大小
            chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
            synchronized (taskInfo) {
              taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
            }
            callback.progress(taskInfo, chunkInfo);
            //分段下载完成关闭fileChannel
            if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
              fileChannel.close();
              ctx.channel().close();
              //分段下载完成回调
              chunkInfo.setStatus(2);
              chunkInfo.setLastTime(System.currentTimeMillis());
              callback.chunkDone(taskInfo, chunkInfo);
              synchronized (taskInfo) {
                if (taskInfo.getStatus() == 1 && taskInfo.getChunkInfoList().stream()
                    .allMatch((chunk) -> chunk.getStatus() == 2)) {
                  //记录完成时间
                  taskInfo.setLastTime(System.currentTimeMillis());
                  if (taskInfo.getTotalSize() <= 0) {  //chunked编码最后更新文件大小
                    taskInfo.setTotalSize(taskInfo.getDownSize());
                    taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
                  }
                  if (taskInfo.getChunkInfoList().size() > 1) {
                    //合并文件
                    taskInfo.setStatus(5);
                    HttpDownUtil.startMerge(taskInfo);
                  }
                  //文件下载完成回调
                  taskInfo.setStatus(2);
                  callback.done(taskInfo);
                }
              }
            } else if (realContentSize == chunkInfo.getDownSize()
                || (realContentSize - 1) == chunkInfo.getDownSize()) {  //百度响应做了手脚，会少一个字节
              //真实响应字节小于要下载的字节，在下载完成后要继续下载
              HttpDownUtil.countinueDown(taskInfo, chunkInfo);
            }
          } else {
            HttpResponse httpResponse = (HttpResponse) msg;
            System.out.println(httpResponse);
            realContentSize = HttpDownUtil.getDownFileSize(httpResponse.headers());
            if (taskInfo.getChunkInfoList().size() > 1) {
              //下载使用同步IO写入，合并使用异步IO减少合并等待时间
              fileChannel = new RandomAccessFile(taskInfo.buildChunkFilePath(chunkInfo.getIndex()),
                  "rws").getChannel();
              fileChannel.position(fileChannel.size());
            } else {
              fileChannel = FileUtil.getRafFile(taskInfo.buildTaskFilePath()).getChannel();
            }
            chunkInfo.setStatus(1);
            chunkInfo.setFileChannel(fileChannel);
            callback.chunkStart(taskInfo, chunkInfo);
          }
        } catch (Exception e) {
          throw e;
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
        super.exceptionCaught(ctx, cause);
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

}
