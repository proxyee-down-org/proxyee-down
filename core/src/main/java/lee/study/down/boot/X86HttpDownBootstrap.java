package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.List;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;

public class X86HttpDownBootstrap extends AbstractHttpDownBootstrap {

  private static final String ATTR_CACHE = "cache";

  public X86HttpDownBootstrap(HttpDownInfo httpDownInfo,
      int retryCount,
      SslContext clientSslContext,
      NioEventLoopGroup clientLoopGroup,
      HttpDownCallback callback,
      TimeoutCheckTask timeoutCheckTask) {
    super(httpDownInfo, retryCount, clientSslContext, clientLoopGroup, callback, timeoutCheckTask);
  }

  @Override
  public void initBoot() throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    if (taskInfo.getChunkInfoList().size() > 1) {
      FileUtil.deleteIfExists(taskInfo.buildChunksPath());
      FileUtil.createDirSmart(taskInfo.buildChunksPath());
      FileUtil.createFile(taskInfo.buildTaskFilePath());
    }
  }

  @Override
  public boolean continueDownHandle() throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    if (taskInfo.getStatus() == HttpDownStatus.MERGE_CANCEL) {
      merge();
    } else if (!FileUtil.exists(taskInfo.buildChunksPath())) {
      close();
      startDown();
    } else {
      return true;
    }
    return false;
  }

  @Override
  public void merge() throws Exception {
    /*TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    String filePath = taskInfo.buildTaskFilePath();
    long position = 0;
    taskInfo.setStatus(HttpDownStatus.MERGE);
    if (getCallback() != null) {
      getCallback().onMerge(getHttpDownInfo());
    }
    synchronized (taskInfo) {
      try (
          FileChannel targetChannel = new RandomAccessFile(filePath, "rw").getChannel()
      ) {
        for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
          try (
              FileChannel chunkChannel = new RandomAccessFile(
                  taskInfo.buildChunkFilePath(chunkInfo.getIndex()), "rw").getChannel()
          ) {
            long remaining = chunkChannel.size();
            while (remaining > 0) {
              long transferred = targetChannel.transferFrom(chunkChannel, position, remaining);
              remaining -= transferred;
              position += transferred;
            }
          }
        }
      }
    }
    FileUtil.deleteIfExists(taskInfo.buildChunksPath());*/
  }

  @Override
  public Closeable[] initFileWriter(ChunkInfo chunkInfo) throws Exception {
    setAttr(chunkInfo, "cache", new LinkedList<ByteBuffer>());
    return null;
  }


  @Override
  public int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    int ret = -1;
    List<ByteBuffer> cache = (List<ByteBuffer>) getAttr(chunkInfo, ATTR_CACHE);
    if (cache != null) {
      cache.add(buffer);
      if (cache.size() == 64) {
        ret = cacheFlush(chunkInfo);
      }
    }
    return ret;
  }

  public int getCacheSize(ChunkInfo chunkInfo) {
    List<ByteBuffer> cache = (List<ByteBuffer>) getAttr(chunkInfo, ATTR_CACHE);
    if (cache != null && cache.size() > 0) {
      return cache.stream().map(bc -> bc.remaining()).reduce((r1, r2) -> r1 + r2).get();
    }
    return 0;
  }

  public int cacheFlush(ChunkInfo chunkInfo) throws IOException {
    int ret = getCacheSize(chunkInfo);
    if (ret > 0) {
      List<ByteBuffer> cache = (List<ByteBuffer>) getAttr(chunkInfo, ATTR_CACHE);
      MappedByteBuffer mappedByteBuffer = new RandomAccessFile(
          getHttpDownInfo().getTaskInfo().buildTaskFilePath(), "rw").getChannel()
          .map(MapMode.READ_WRITE, chunkInfo.getNowStartPosition() + chunkInfo.getDownSize(),
              ret);
      cache.forEach(bc -> mappedByteBuffer.put(bc));
      cache.clear();
      FileUtil.unmap(mappedByteBuffer);
    }
    return ret;
  }
}
