package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.io.Mmap;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;

public class X64HttpDownBootstrap extends AbstractHttpDownBootstrap {

  private Mmap mmap;

  public X64HttpDownBootstrap(HttpDownInfo httpDownInfo,
      int retryCount,
      SslContext clientSslContext,
      NioEventLoopGroup clientLoopGroup,
      HttpDownCallback callback,
      TimeoutCheckTask timeoutCheckTask) {
    super(httpDownInfo, retryCount, clientSslContext, clientLoopGroup, callback, timeoutCheckTask);
  }

  @Override
  public boolean continueDownHandle() throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    if (!FileUtil.exists(taskInfo.buildTaskFilePath())) {
      close();
      startDown();
      return false;
    }
    return true;
  }

  @Override
  public void merge() throws Exception {

  }

  @Override
  public void initBoot() throws IOException {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    try (
        RandomAccessFile randomAccessFile = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw")
    ) {
      randomAccessFile.setLength(taskInfo.getTotalSize());
    }
  }

  @Override
  public Closeable[] initFileWriter(ChunkInfo chunkInfo) throws Exception {
    if (mmap == null) {
      mmap = new Mmap(getHttpDownInfo().getTaskInfo().buildTaskFilePath(),
          getHttpDownInfo().getTaskInfo().getTotalSize());
    }
    return null;
  }

  @Override
  public int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    int ret = -1;
    if (mmap != null) {
      ret = buffer.remaining();
      byte[] bts = new byte[buffer.remaining()];
      buffer.get(bts);
      mmap.putBytes(chunkInfo.getNowStartPosition() + chunkInfo.getDownSize(), bts);
    }
    return ret;
  }

  @Override
  public void close() {
    super.close();
    if (mmap != null) {
      try {
        mmap.close();
        mmap = null;
      } catch (IOException e) {
        LOGGER.error("mmap close error", e);
      }
    }
  }
}
