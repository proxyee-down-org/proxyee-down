package lee.study.down;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.ssl.SslContext;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.handle.HttpDownInitializer;
import lee.study.down.io.LargeMappedByteBuffer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@AllArgsConstructor
public class HttpDownBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownBootstrap.class);

  private HttpDownInfo httpDownInfo;
  private SslContext clientSslContext;
  private NioEventLoopGroup clientLoopGroup;
  private HttpDownCallback callback;

  public void startDown() throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    FileUtil.deleteIfExists(taskInfo.buildTaskFilePath());
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw")
    ) {
      randomAccessFile.setLength(taskInfo.getTotalSize());
    }
    //文件下载开始回调
    taskInfo.reset();
    taskInfo.setStatus(HttpDownStatus.RUNNING);
    taskInfo.setStartTime(System.currentTimeMillis());
    for (int i = 0; i < taskInfo.getChunkInfoList().size(); i++) {
      ChunkInfo chunkInfo = taskInfo.getChunkInfoList().get(i);
      //设置状态和时间
      chunkInfo.setStartTime(System.currentTimeMillis());
      startChunkDown(chunkInfo, HttpDownStatus.CONNECTING_NORMAL);
    }
    callback.onStart(httpDownInfo);
  }

  public void startChunkDown(ChunkInfo chunkInfo, int updateStatus) {
    HttpRequestInfo requestInfo = (HttpRequestInfo) httpDownInfo.getRequest();
    RequestProto requestProto = requestInfo.requestProto();
    LOGGER.debug("开始下载：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
    ChannelFuture cf = new Bootstrap().group(clientLoopGroup).handler(
        new HttpDownInitializer(requestProto.getSsl(), this, chunkInfo))
        .connect(requestProto.getHost(), requestProto.getPort());
    chunkInfo.setStatus(updateStatus);
    cf.addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        synchronized (requestInfo) {
          LOGGER.debug("下载连接成功：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
          if (httpDownInfo.getTaskInfo().isSupportRange()) {
            requestInfo.headers()
                .set(HttpHeaderNames.RANGE,
                    "bytes=" + chunkInfo.getNowStartPosition() + "-" + chunkInfo.getEndPosition());
          } else {
            requestInfo.headers().remove(HttpHeaderNames.RANGE);
          }
          future.channel().writeAndFlush(httpDownInfo.getRequest());
        }
        if (requestInfo.content() != null) {
          //请求体写入
          HttpContent content = new DefaultLastHttpContent();
          content.content().writeBytes(requestInfo.content());
          future.channel().writeAndFlush(content);
        }
      } else {
        LOGGER.debug("下载连接失败：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
        chunkInfo.setStatus(HttpDownStatus.FAIL);
        future.channel().close();
      }
    });
  }

  /**
   * 下载重试
   */
  public void retryChunkDown(ChunkInfo chunkInfo, int updateStatus)
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (chunkInfo) {
      closeChunk(chunkInfo);
      //已经下载完成
      if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
        chunkInfo.setStatus(HttpDownStatus.DONE);
        callback.onChunkDone(httpDownInfo, chunkInfo);
        return;
      }
      if (taskInfo.isSupportRange()) {
        chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
      }
      startChunkDown(chunkInfo, updateStatus);
    }
  }

  /**
   * 下载重试
   */
  public void retryChunkDown(ChunkInfo chunkInfo) throws Exception {
    retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_FAIL);
  }

  /**
   * 暂停下载
   */
  public void pauseDown() throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      taskInfo.setStatus(HttpDownStatus.PAUSE);
      for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
        synchronized (chunkInfo) {
          closeChunk(chunkInfo);
          if (chunkInfo.getStatus() != HttpDownStatus.DONE) {
            chunkInfo.setStatus(HttpDownStatus.PAUSE);
          }
        }
      }
    }
    callback.onPause(getHttpDownInfo());
  }

  /**
   * 继续下载
   */
  public void continueDown()
      throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    synchronized (taskInfo) {
      //如果文件被删除重新开始下载
      if (!FileUtil.exists(taskInfo.buildTaskFilePath())) {
        close();
        startDown();
      } else {
        taskInfo.setStatus(1);
        taskInfo.setPauseTime(
            taskInfo.getPauseTime() + (System.currentTimeMillis() - taskInfo.getLastTime()));
        for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
          synchronized (chunkInfo) {
            if (chunkInfo.getStatus() == HttpDownStatus.PAUSE) {
              chunkInfo.setPauseTime(taskInfo.getPauseTime());
              retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_NORMAL);
            }
          }
        }
      }
    }
    callback.onContinue(getHttpDownInfo());
  }

  public void closeChunk(ChunkInfo chunkInfo) {
    try {
      HttpDownUtil.safeClose(chunkInfo.getChannel(), chunkInfo.getFileChannel(),
          chunkInfo.getMappedBuffer());
    } catch (Exception e) {
      LOGGER.error("closeChunk error", e);
    }
  }

  public void close() {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      for (ChunkInfo chunkInfo : httpDownInfo.getTaskInfo().getChunkInfoList()) {
        synchronized (chunkInfo) {
          closeChunk(chunkInfo);
        }
      }
    }
  }
}
