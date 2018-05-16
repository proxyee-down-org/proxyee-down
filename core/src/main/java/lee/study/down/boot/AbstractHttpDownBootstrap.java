package lee.study.down.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.ssl.SslContext;
import io.netty.resolver.NoopAddressResolverGroup;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.exception.BootstrapException;
import lee.study.down.handle.HttpDownInitializer;
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
public abstract class AbstractHttpDownBootstrap {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpDownBootstrap.class);

  //tcp bufferSize最大为128K
  public static final int BUFFER_SIZE = 1024 * 128;
  private static final RecvByteBufAllocator RECV_BYTE_BUF_ALLOCATOR = new AdaptiveRecvByteBufAllocator(
      64, BUFFER_SIZE, BUFFER_SIZE);

  protected static final String ATTR_CHANNEL = "channel";
  protected static final String ATTR_FILE_CLOSEABLE = "fileCloseable";

  private HttpDownInfo httpDownInfo;
  private int retryCount;
  private SslContext clientSslContext;
  private HttpDownCallback callback;
  private TimeoutCheckTask timeoutCheckTask;
  private final Map<Integer, Map<String, Object>> attr = new HashMap<>();

  public AbstractHttpDownBootstrap(HttpDownInfo httpDownInfo, int retryCount, SslContext clientSslContext, HttpDownCallback callback, TimeoutCheckTask timeoutCheckTask) {
    this.httpDownInfo = httpDownInfo;
    this.retryCount = retryCount;
    this.clientSslContext = clientSslContext;
    this.callback = callback;
    this.timeoutCheckTask = timeoutCheckTask;
  }

  private NioEventLoopGroup clientLoopGroup;

  public void startDown() throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    taskInfo.buildChunkInfoList();
    if (!FileUtil.exists(taskInfo.getFilePath())) {
      FileUtil.createDirSmart(taskInfo.getFilePath());
    }
    if (!FileUtil.canWrite(taskInfo.getFilePath())) {
      throw new BootstrapException("无权访问下载路径，请修改路径或开放目录写入权限");
    }
    //磁盘空间不足
    if (taskInfo.getTotalSize() > FileUtil.getDiskFreeSize(taskInfo.getFilePath())) {
      throw new BootstrapException("磁盘空间不足，请修改路径");
    }
    //有文件同名
    if (new File(taskInfo.buildTaskFilePath()).exists()) {
      throw new BootstrapException("文件名已存在，请修改文件名");
    }
    //创建文件
    FileUtil.createSparseFile(taskInfo.buildTaskFilePath(), taskInfo.getTotalSize());
    //文件下载开始回调
    taskInfo.reset();
    taskInfo.setStatus(HttpDownStatus.RUNNING);
    taskInfo.setStartTime(System.currentTimeMillis());
    clientLoopGroup = new NioEventLoopGroup(1);
    for (int i = 0; i < taskInfo.getChunkInfoList().size(); i++) {
      ChunkInfo chunkInfo = taskInfo.getChunkInfoList().get(i);
      //设置状态和时间
      chunkInfo.setStartTime(System.currentTimeMillis());
      startChunkDown(chunkInfo, HttpDownStatus.CONNECTING_NORMAL);
    }
    if (callback != null) {
      callback.onStart(httpDownInfo);
    }
  }

  protected void startChunkDown(ChunkInfo chunkInfo, int updateStatus) throws Exception {
    HttpRequestInfo requestInfo = (HttpRequestInfo) httpDownInfo.getRequest();
    RequestProto requestProto = requestInfo.requestProto();
    LOGGER.debug("开始下载：" + chunkInfo);
    Bootstrap bootstrap = new Bootstrap()
        .channel(NioSocketChannel.class)
        .option(ChannelOption.RCVBUF_ALLOCATOR, RECV_BYTE_BUF_ALLOCATOR)
        .option(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
        .group(clientLoopGroup)
        .handler(new HttpDownInitializer(requestProto.getSsl(), this, chunkInfo));
    if (httpDownInfo.getProxyConfig() != null) {
      //代理服务器解析DNS和连接
      bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
    }
    if (callback != null) {
      callback.onChunkConnecting(httpDownInfo, chunkInfo);
    }
    ChannelFuture cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
    chunkInfo.setStatus(updateStatus);
    //重置最后下载时间
    chunkInfo.setLastDownTime(System.currentTimeMillis());
    cf.addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        synchronized (chunkInfo) {
          setChannel(chunkInfo, future.channel());
        }
        synchronized (requestInfo) {
          LOGGER.debug("下载连接成功：channelId[" + future.channel().id() + "]\t" + chunkInfo);
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
        LOGGER.debug("下载连接失败：" + chunkInfo);
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
      close(chunkInfo);
      //已经下载完成
      if (chunkInfo.getDownSize() == chunkInfo.getTotalSize()) {
        chunkInfo.setStatus(HttpDownStatus.DONE);
        if (callback != null) {
          callback.onChunkDone(httpDownInfo, chunkInfo);
        }
        return;
      }
      if (taskInfo.isSupportRange()) {
        chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
      }
      if (chunkInfo.getErrorCount() < retryCount) {
        startChunkDown(chunkInfo, updateStatus);
      } else {
        if (taskInfo.getChunkInfoList().stream()
            .filter((chunk) -> chunk.getStatus() != HttpDownStatus.DONE)
            .allMatch((chunk) -> chunk.getErrorCount() >= retryCount)) {
          taskInfo.setStatus(HttpDownStatus.FAIL);
          if (callback != null) {
            callback.onError(httpDownInfo, null);
          }
        }
      }

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
      if (taskInfo.getStatus() == HttpDownStatus.PAUSE
          || taskInfo.getStatus() == HttpDownStatus.DONE) {
        return;
      }
      taskInfo.setStatus(HttpDownStatus.PAUSE);
      for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
        synchronized (chunkInfo) {
          if (chunkInfo.getStatus() != HttpDownStatus.DONE) {
            chunkInfo.setStatus(HttpDownStatus.PAUSE);
          }
        }
      }
      close();
    }
    if (callback != null) {
      callback.onPause(httpDownInfo);
    }
  }

  /**
   * 继续下载
   */
  public void continueDown()
      throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      if (taskInfo.getStatus() == HttpDownStatus.RUNNING
          || taskInfo.getStatus() == HttpDownStatus.DONE) {
        return;
      }
      if (!FileUtil.exists(taskInfo.buildTaskFilePath())) {
        close();
        startDown();
      } else {
        taskInfo.setStatus(HttpDownStatus.RUNNING);
        taskInfo.getChunkInfoList().forEach((chunk) -> chunk.setErrorCount(0));
        long curTime = System.currentTimeMillis();
        taskInfo.setPauseTime(
            taskInfo.getPauseTime() + (curTime - taskInfo.getLastTime()));
        taskInfo.setLastTime(curTime);
        clientLoopGroup = new NioEventLoopGroup(1);
        for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
          synchronized (chunkInfo) {
            if (chunkInfo.getStatus() == HttpDownStatus.PAUSE
                || chunkInfo.getStatus() == HttpDownStatus.CONNECTING_FAIL) {
              chunkInfo.setPauseTime(taskInfo.getPauseTime());
              chunkInfo.setLastTime(curTime);
              retryChunkDown(chunkInfo, HttpDownStatus.CONNECTING_NORMAL);
            }
          }
        }
      }
    }
    if (callback != null) {
      callback.onContinue(httpDownInfo);
    }
  }

  public void close(ChunkInfo chunkInfo) {
    close(chunkInfo, -1);
  }

  public void close(ChunkInfo chunkInfo, int status) {
    try {
      if (status != -1) {
        chunkInfo.setStatus(status);
      }
      if (!attr.containsKey(chunkInfo.getIndex())) {
        return;
      }
      Channel channel = getChannel(chunkInfo);
      LOGGER.debug(
          "下载连接关闭：channelId[" + (channel != null ? channel.id() : "null") + "]\t" + chunkInfo);
      HttpDownUtil.safeClose(channel);
      Closeable closeable = (Closeable) getAttr(chunkInfo, ATTR_FILE_CLOSEABLE);
      if (closeable != null) {
        closeable.close();
      }
      attr.remove(chunkInfo.getIndex());
    } catch (Exception e) {
      LOGGER.error("closeChunk error", e);
    }
  }

  public void close(int status) {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      if (status != -1) {
        taskInfo.setStatus(status);
      }
      for (ChunkInfo chunkInfo : httpDownInfo.getTaskInfo().getChunkInfoList()) {
        synchronized (chunkInfo) {
          close(chunkInfo, status);
        }
      }
    }
    if (clientLoopGroup != null) {
      clientLoopGroup.shutdownGracefully();
    }
  }

  public void close() {
    close(-1);
  }

  public void delete(boolean delFile) throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    //删除任务进度记录文件
    synchronized (taskInfo) {
      close(HttpDownStatus.WAIT);
      timeoutCheckTask.delBoot(httpDownInfo.getTaskInfo().getId());
      FileUtil.deleteIfExists(taskInfo.buildTaskRecordFilePath());
      FileUtil.deleteIfExists(taskInfo.buildTaskRecordBakFilePath());
      if (delFile) {
        FileUtil.deleteIfExists(taskInfo.buildTaskFilePath());
      }
      if (callback != null) {
        callback.onDelete(httpDownInfo);
      }
    }
  }

  protected void setAttr(ChunkInfo chunkInfo, String key, Object object) {
    Map<String, Object> map = attr.get(chunkInfo.getIndex());
    if (map == null) {
      map = new HashMap<>();
      attr.put(chunkInfo.getIndex(), map);
    }
    map.put(key, object);
  }

  protected Object getAttr(ChunkInfo chunkInfo, String key) {
    Map<String, Object> map = attr.get(chunkInfo.getIndex());
    if (map == null) {
      return null;
    } else {
      return map.get(key);
    }
  }

  protected void setChannel(ChunkInfo chunkInfo, Channel channel) {
    setAttr(chunkInfo, ATTR_CHANNEL, channel);
  }

  public Channel getChannel(ChunkInfo chunkInfo) {
    return (Channel) getAttr(chunkInfo, ATTR_CHANNEL);
  }

  public abstract int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer)
      throws IOException;

  public Closeable initFileWriter(ChunkInfo chunkInfo) throws Exception {
    return null;
  }
}
