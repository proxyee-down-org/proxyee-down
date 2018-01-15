package lee.study.down.handle;

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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import lee.study.down.HttpDownBootstrap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.io.LargeMappedByteBuffer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.proxy.ProxyHandleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownInitializer extends ChannelInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownInitializer.class);

  private boolean isSsl;
  private HttpDownBootstrap bootstrap;
  private ChunkInfo chunkInfo;

  private long realContentSize;

  public HttpDownInitializer(boolean isSsl, HttpDownBootstrap bootstrap,
      ChunkInfo chunkInfo) {
    this.isSsl = isSsl;
    this.bootstrap = bootstrap;
    this.chunkInfo = chunkInfo;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    if (bootstrap.getHttpDownInfo().getProxyConfig() != null) {
      ch.pipeline().addLast(ProxyHandleFactory.build(bootstrap.getHttpDownInfo().getProxyConfig()));
    }
    if (isSsl) {
      ch.pipeline().addLast(bootstrap.getClientSslContext().newHandler(ch.alloc()));
    }
    ch.pipeline()
        .addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

      private FileChannel fileChannel;
      private LargeMappedByteBuffer mappedBuffer;
      private TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
      private HttpDownCallback callback = bootstrap.getCallback();

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
          if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf byteBuf = httpContent.content();
            int readableBytes = byteBuf.readableBytes();
            synchronized (chunkInfo) {
              Channel nowChannel = (Channel) bootstrap
                  .getAttr(chunkInfo, HttpDownBootstrap.ATTR_CHANNEL);
              LargeMappedByteBuffer nowMapBuffer = (LargeMappedByteBuffer) bootstrap
                  .getAttr(chunkInfo, HttpDownBootstrap.ATTR_MAP_BUFFER);
              if (chunkInfo.getStatus() == HttpDownStatus.RUNNING
                  && nowChannel == ctx.channel()) {
                nowMapBuffer.put(byteBuf.nioBuffer());
                //文件已下载大小
                chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
                taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
                callback.onProgress(bootstrap.getHttpDownInfo(), chunkInfo);
              } else {
                safeClose(ctx.channel());
                return;
              }
            }
            if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
              bootstrap.close(chunkInfo);
              //分段下载完成回调
              chunkInfo.setStatus(HttpDownStatus.DONE);
              chunkInfo.setLastTime(System.currentTimeMillis());
              LOGGER.debug("分段下载完成：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize() + "\t"
                  + taskInfo.getStatus());
              callback.onChunkDone(bootstrap.getHttpDownInfo(), chunkInfo);
              synchronized (taskInfo) {
                if (taskInfo.getStatus() == HttpDownStatus.RUNNING
                    && taskInfo.getChunkInfoList().stream()
                    .allMatch((chunk) -> chunk.getStatus() == HttpDownStatus.DONE)) {
                  //记录完成时间
                  taskInfo.setLastTime(System.currentTimeMillis());
                  if (taskInfo.getTotalSize() <= 0) {  //chunked编码最后更新文件大小
                    taskInfo.setTotalSize(taskInfo.getDownSize());
                    taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
                  }
                  //文件下载完成回调
                  taskInfo.setStatus(HttpDownStatus.DONE);
                  LOGGER.debug("下载完成：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                  callback.onDone(bootstrap.getHttpDownInfo());
                }
              }
            } else if (realContentSize
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition() || (realContentSize - 1)
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition()) {  //百度响应做了手脚，会少一个字节
              //真实响应字节小于要下载的字节，在下载完成后要继续下载
              LOGGER.debug("继续下载：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
              bootstrap.retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_CONTINUE);
            }
          } else {
            HttpResponse httpResponse = (HttpResponse) msg;
            realContentSize = HttpDownUtil.getDownFileSize(httpResponse.headers());
            LOGGER.debug(
                "下载响应：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize() + "\t"
                    + httpResponse.headers().get(
                    HttpHeaderNames.CONTENT_RANGE) + "\t" + realContentSize);
            synchronized (chunkInfo) {
              //判断是否为状态是否为连接中
              if (chunkInfo.getStatus() == HttpDownStatus.CONNECTING_NORMAL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_FAIL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_CONTINUE) {
                fileChannel = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw")
                    .getChannel();
                mappedBuffer = new LargeMappedByteBuffer(fileChannel,
                    MapMode.READ_WRITE, chunkInfo.getOriStartPosition() + chunkInfo.getDownSize(),
                    chunkInfo.getTotalSize() - chunkInfo.getDownSize());
                chunkInfo.setStatus(HttpDownStatus.RUNNING);
                bootstrap.setAttr(chunkInfo, HttpDownBootstrap.ATTR_CHANNEL, ctx.channel());
                bootstrap.setAttr(chunkInfo, HttpDownBootstrap.ATTR_FILE_CHANNEL, fileChannel);
                bootstrap.setAttr(chunkInfo, HttpDownBootstrap.ATTR_MAP_BUFFER, mappedBuffer);
                callback.onChunkStart(bootstrap.getHttpDownInfo(), chunkInfo);
              } else {
                bootstrap.close(chunkInfo);
              }
            }
          }
        } catch (Exception e) {
          throw e;
        } finally {
          ReferenceCountUtil.release(msg);
        }
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("down onError:", cause);
        Channel nowChannel = (Channel) bootstrap
            .getAttr(chunkInfo, HttpDownBootstrap.ATTR_CHANNEL);
        if (nowChannel == ctx.channel()) {
          chunkInfo.setStatus(HttpDownStatus.CONNECTING_FAIL);
          bootstrap.retryChunkDown(chunkInfo);
          callback.onError(bootstrap.getHttpDownInfo(), chunkInfo, cause);
        } else {
          safeClose(ctx.channel());
        }
      }

      @Override
      public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        bootstrap.close(chunkInfo);
      }

      private void safeClose(Channel channel) {
        try {
          HttpDownUtil.safeClose(channel, fileChannel, mappedBuffer);
        } catch (IOException e) {
          LOGGER.error("connect close fail:", e);
        }
      }

    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    LOGGER.error("down onInit:", cause);
  }
}
