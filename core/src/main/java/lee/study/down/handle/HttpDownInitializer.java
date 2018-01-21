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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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
              if (chunkInfo.getStatus() == HttpDownStatus.RUNNING
                  && nowChannel == ctx.channel()
                  && mappedBuffer != null) {
                mappedBuffer.put(byteBuf.nioBuffer());
                //文件已下载大小
                chunkInfo.setDownSize(chunkInfo.getDownSize() + readableBytes);
                taskInfo.setDownSize(taskInfo.getDownSize() + readableBytes);
                if (callback != null) {
                  callback.onProgress(bootstrap.getHttpDownInfo(), chunkInfo);
                }
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
              LOGGER.debug("分段下载完成：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
              taskInfo.refresh(chunkInfo);
              if (callback != null) {
                callback.onChunkDone(bootstrap.getHttpDownInfo(), chunkInfo);
              }
              synchronized (taskInfo) {
                if (taskInfo.getStatus() == HttpDownStatus.RUNNING
                    && taskInfo.getChunkInfoList().stream()
                    .allMatch((chunk) -> chunk.getStatus() == HttpDownStatus.DONE)) {
                  if (taskInfo.getTotalSize() <= 0) {  //chunked编码最后更新文件大小
                    taskInfo.setTotalSize(taskInfo.getDownSize());
                    taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
                  }
                  //文件下载完成回调
                  taskInfo.setStatus(HttpDownStatus.DONE);
                  LOGGER.debug("下载完成：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
                  if (callback != null) {
                    callback.onDone(bootstrap.getHttpDownInfo());
                  }
                }
              }
            } else if (realContentSize
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition() || (realContentSize - 1)
                == chunkInfo.getDownSize() + chunkInfo.getOriStartPosition() - chunkInfo
                .getNowStartPosition()) {  //百度响应做了手脚，会少一个字节
              //真实响应字节小于要下载的字节，在下载完成后要继续下载
              LOGGER.debug("继续下载：channelId[" + ctx.channel().id() + "]\t" + chunkInfo);
              bootstrap.retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_CONTINUE);
            }
          } else {
            HttpResponse httpResponse = (HttpResponse) msg;
            if ((httpResponse.status().code() + "").indexOf("20") != 0) {
              throw new RuntimeException("http down response error:" + httpResponse);
            }
            realContentSize = HttpDownUtil.getDownFileSize(httpResponse.headers());
            synchronized (chunkInfo) {
              //判断状态是否为连接中
              if (chunkInfo.getStatus() == HttpDownStatus.CONNECTING_NORMAL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_FAIL
                  || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_CONTINUE) {
                LOGGER.debug(
                    "下载响应：channelId[" + ctx.channel().id() + "]\t contentSize[" + realContentSize
                        + "]" + chunkInfo);
                fileChannel = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw")
                    .getChannel();
                mappedBuffer = new LargeMappedByteBuffer(fileChannel,
                    MapMode.READ_WRITE, chunkInfo.getNowStartPosition(),
                    chunkInfo.getEndPosition() - chunkInfo.getNowStartPosition() + 1);
                chunkInfo.setStatus(HttpDownStatus.RUNNING);
                chunkInfo
                    .setDownSize(chunkInfo.getNowStartPosition() - chunkInfo.getOriStartPosition());
                bootstrap.setAttr(chunkInfo, HttpDownBootstrap.ATTR_FILE_CHANNEL, fileChannel);
                bootstrap.setAttr(chunkInfo, HttpDownBootstrap.ATTR_MAP_BUFFER, mappedBuffer);
                if (callback != null) {
                  callback.onChunkStart(bootstrap.getHttpDownInfo(), chunkInfo);
                }
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
        LOGGER.error("down onError:", cause);
        Channel nowChannel = (Channel) bootstrap
            .getAttr(chunkInfo, HttpDownBootstrap.ATTR_CHANNEL);
        safeClose(ctx.channel());
        if (nowChannel == ctx.channel()) {
          chunkInfo.setStatus(HttpDownStatus.CONNECTING_FAIL);
          bootstrap.retryChunkDown(chunkInfo);
          if (callback != null) {
            callback.onError(bootstrap.getHttpDownInfo(), chunkInfo, cause);
          }
        }
      }

      @Override
      public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        safeClose(ctx.channel());
      }

      private void safeClose(Channel channel) {
        try {
          HttpDownUtil.safeClose(channel, fileChannel, mappedBuffer);
        } catch (IOException e) {
          LOGGER.error("safeClose fail:", e);
        }
      }

    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    LOGGER.error("down onInit:", cause);
  }

  public static void main(String[] args) throws Exception {
    /*FileChannel fileChannel = new RandomAccessFile("f:/down/【批量下载】新建文本文档等22.zip","rw").getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate(130556);
    fileChannel.position(3821941867L);
    fileChannel.read(byteBuffer);
    byteBuffer.flip();
    FileChannel fileChannel2 = new RandomAccessFile("f:/down/1.txt","rw").getChannel();
    fileChannel2.write(byteBuffer);*/
    FileChannel fileChannel = new RandomAccessFile("f:/idm/【批量下载】新建文本文档等.zip", "rw").getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate(130556);
    fileChannel.position(3821941867L);
    fileChannel.read(byteBuffer);
    byteBuffer.flip();
    FileChannel fileChannel2 = new RandomAccessFile("f:/down/2.txt", "rw").getChannel();
    fileChannel2.write(byteBuffer);
    //3821941867
    //3821941867-3858226053
    /*System.out.println((1674458220L+Integer.MAX_VALUE)/(4658990011L/128));
    System.out.println(1674458220L+Integer.MAX_VALUE);*/
    /*long size = 4658990011L;
    MappedByteBuffer input1 = new RandomAccessFile("f:/down/【批量下载】新建文本文档等22.zip","rw").getChannel().map(MapMode.READ_WRITE,0,Integer.MAX_VALUE);
    MappedByteBuffer input2 = new RandomAccessFile("f:/idm/【批量下载】新建文本文档等.zip","rw").getChannel().map(MapMode.READ_WRITE,0,Integer.MAX_VALUE);
    for (long i = 0; i < Integer.MAX_VALUE; i++) {
      if (input1.get() != input2.get()) {
        System.out.println("1:"+i);
        return;
      }
    }
    FileUtil.unmap(input1);
    FileUtil.unmap(input2);
    input1 = new RandomAccessFile("f:/down/【批量下载】新建文本文档等22.zip","rw").getChannel().map(MapMode.READ_WRITE,Integer.MAX_VALUE,Integer.MAX_VALUE);
    input2 = new RandomAccessFile("f:/idm/【批量下载】新建文本文档等.zip","rw").getChannel().map(MapMode.READ_WRITE,Integer.MAX_VALUE,Integer.MAX_VALUE);
    for (long i = 0; i < Integer.MAX_VALUE; i++) {
      if (input1.get() != input2.get()) {
        System.out.println("2:"+i);
        return;
      }
    }
    FileUtil.unmap(input1);
    FileUtil.unmap(input2);
    input1 = new RandomAccessFile("f:/down/【批量下载】新建文本文档等22.zip","rw").getChannel().map(MapMode.READ_WRITE,Integer.MAX_VALUE*2L,size-Integer.MAX_VALUE*2L);
    input2 = new RandomAccessFile("f:/idm/【批量下载】新建文本文档等.zip","rw").getChannel().map(MapMode.READ_WRITE,Integer.MAX_VALUE*2L,size-Integer.MAX_VALUE*2L);
    for (long i = 0; i < Integer.MAX_VALUE; i++) {
      if (input1.get() != input2.get()) {
        System.out.println("3:"+i);
        return;
      }
    }*/
  }
}
