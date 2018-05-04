package lee.study.down.handle;

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
import java.io.Closeable;
import java.io.IOException;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.proxy.ProxyHandleFactory;
import lee.study.proxyee.util.ProtoUtil.RequestProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownInitializer extends ChannelInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownInitializer.class);

  private boolean isSsl;
  private AbstractHttpDownBootstrap bootstrap;
  private ChunkInfo chunkInfo;

  private long realContentSize;
  private boolean isSucc;

  public HttpDownInitializer(boolean isSsl, AbstractHttpDownBootstrap bootstrap,
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
      RequestProto requestProto = ((HttpRequestInfo) bootstrap.getHttpDownInfo().getRequest()).requestProto();
      ch.pipeline().addLast(bootstrap.getClientSslContext().newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
    }
    ch.pipeline()
        .addLast("httpCodec",
            new HttpClientCodec(4096, 8192, AbstractHttpDownBootstrap.BUFFER_SIZE));
    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

      private Closeable closeable;
      private TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
      private HttpDownCallback callback = bootstrap.getCallback();

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
          if (msg instanceof HttpContent) {
            if (!isSucc) {
              return;
            }
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf byteBuf = httpContent.content();
            synchronized (chunkInfo) {
              Channel nowChannel = bootstrap.getChannel(chunkInfo);
              if (chunkInfo.getStatus() == HttpDownStatus.RUNNING
                  && nowChannel == ctx.channel()) {
                int readableBytes = bootstrap.doFileWriter(chunkInfo, byteBuf.nioBuffer());
                if (readableBytes > 0) {
                  //最后一次下载时间
                  chunkInfo.setLastDownTime(System.currentTimeMillis());
                  //文件已下载大小
                  chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
                  taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
                  if (callback != null) {
                    callback.onProgress(bootstrap.getHttpDownInfo(), chunkInfo);
                  }
                }
              } else {
                safeClose(ctx.channel());
                return;
              }
            }
            if (isDone(chunkInfo.getDownSize(), httpContent)) {
              LOGGER.debug("分段下载完成：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
              bootstrap.close(chunkInfo);
              //分段下载完成回调
              chunkInfo.setStatus(HttpDownStatus.DONE);
              taskInfo.refresh(chunkInfo);
              if (callback != null) {
                callback.onChunkDone(bootstrap.getHttpDownInfo(), chunkInfo);
              }
              synchronized (taskInfo) {
                if (taskInfo.getStatus() == HttpDownStatus.RUNNING
                    && taskInfo.getChunkInfoList().stream()
                    .allMatch((chunk) -> chunk.getStatus() == HttpDownStatus.DONE)) {
                  if (!taskInfo.isSupportRange()) {  //chunked编码最后更新文件大小
                    taskInfo.setTotalSize(taskInfo.getDownSize());
                    taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
                  }
                  //文件下载完成回调
                  taskInfo.setStatus(HttpDownStatus.DONE);
                  LOGGER.debug("下载完成：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
                  bootstrap.close();
                  if (callback != null) {
                    callback.onDone(bootstrap.getHttpDownInfo());
                  }
                }
              }
            } else if (isContinue(chunkInfo.getDownSize())) {  //百度响应做了手脚，会少一个字节
              //真实响应字节小于要下载的字节，在下载完成后要继续下载
              LOGGER.debug("继续下载：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
              bootstrap.retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_CONTINUE);
            } else if (chunkInfo.getDownSize() > chunkInfo.getTotalSize()) {
              LOGGER.debug("分段下载异常：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
              chunkInfo.setDownSize(0);
              chunkInfo.setStartTime(System.currentTimeMillis());
              chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition());
              bootstrap.retryChunkDown(chunkInfo);
            }
          } else {
            HttpResponse httpResponse = (HttpResponse) msg;
            Integer responseCode = httpResponse.status().code();
            if (responseCode.toString().indexOf("20") != 0) {
              //应对百度近期同一时段多个连接返回400的问题
              LOGGER.warn(
                  "响应状态码异常：" + responseCode + "\t" + chunkInfo);
              if (responseCode == 401 || responseCode == 403 || responseCode == 404) {
                chunkInfo.setStatus(HttpDownStatus.ERROR_WAIT_CONNECT);
              }
              return;
            }
            realContentSize = HttpDownUtil.getDownContentSize(httpResponse.headers());
            synchronized (chunkInfo) {
              //判断状态是否为连接中
              if (chunkInfo.getStatus() == HttpDownStatus.CONNECTING_NORMAL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_FAIL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_CONTINUE) {
                LOGGER.debug(
                    "下载响应：channelId[" + ctx.channel().id() + "]\t contentSize[" + realContentSize
                        + "]" + chunkInfo);
                chunkInfo
                    .setDownSize(chunkInfo.getNowStartPosition() - chunkInfo.getOriStartPosition());
                closeable = bootstrap.initFileWriter(chunkInfo);
                chunkInfo.setStatus(HttpDownStatus.RUNNING);
                if (callback != null) {
                  callback.onChunkConnected(bootstrap.getHttpDownInfo(), chunkInfo);
                }
                isSucc = true;
              } else {
                safeClose(ctx.channel());
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
        LOGGER.error("down onChunkError:", cause);
        Channel nowChannel = bootstrap.getChannel(chunkInfo);
        safeClose(ctx.channel());
        if (nowChannel == ctx.channel()) {
          if (callback != null) {
            callback.onChunkError(bootstrap.getHttpDownInfo(), chunkInfo, cause);
          }
          bootstrap.retryChunkDown(chunkInfo);
        }
      }

      @Override
      public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        safeClose(ctx.channel());
      }

      private void safeClose(Channel channel) {
        try {
          HttpDownUtil.safeClose(channel, closeable);
        } catch (IOException e) {
          LOGGER.error("safeClose fail:", e);
        }
      }

      private boolean isDone(long downSize, HttpContent content) {
        return downSize == chunkInfo.getTotalSize()
            || (!taskInfo.isSupportRange() && content instanceof LastHttpContent);
      }

      private boolean isContinue(long downSize) {
        long downChunkSize =
            downSize + chunkInfo.getOriStartPosition() - chunkInfo.getNowStartPosition();
        return realContentSize == downChunkSize
            || (realContentSize - 1) == downChunkSize;
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    LOGGER.error("down onInit:", cause);
  }
}
