package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;

public class X86HttpDownBootstrap extends AbstractHttpDownBootstrap {

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
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
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
    FileUtil.deleteIfExists(taskInfo.buildChunksPath());
  }

  @Override
  public Closeable[] initFileWriter(ChunkInfo chunkInfo) throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    FileChannel fileChannel;
    if (taskInfo.getChunkInfoList().size() > 1) {
      fileChannel = new RandomAccessFile(taskInfo.buildChunkFilePath(chunkInfo.getIndex()), "rw")
          .getChannel();
    } else {
      fileChannel = new RandomAccessFile(taskInfo.buildTaskFilePath(), "rw").getChannel();
    }
    fileChannel.position(chunkInfo.getDownSize());
    Closeable[] fileChannels = new Closeable[]{fileChannel};
    setAttr(chunkInfo, ATTR_FILE_CHANNELS, fileChannels);
    return fileChannels;
  }

  @Override
  public boolean doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    Closeable[] fileChannels = getFileWriter(chunkInfo);
    if (fileChannels != null && fileChannels.length > 0) {
      FileChannel fileChannel = (FileChannel) getFileWriter(chunkInfo)[0];
      if (fileChannel != null && fileChannel.isOpen()) {
        fileChannel.write(buffer);
        return true;
      }
    }
    return false;
  }
}
