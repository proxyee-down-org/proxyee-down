package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.io.LargeMappedByteBuffer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;

public class X32HttpDownBootstrap extends AbstractHttpDownBootstrap {

  public X32HttpDownBootstrap(HttpDownInfo httpDownInfo,
      SslContext clientSslContext,
      NioEventLoopGroup clientLoopGroup,
      HttpDownCallback callback) {
    super(httpDownInfo, clientSslContext, clientLoopGroup, callback);
  }

  @Override
  public void initBoot() throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    FileUtil.deleteIfExists(taskInfo.buildChunksPath());
    FileUtil.createDirSmart(taskInfo.buildChunksPath());
  }

  @Override
  public void merge() throws Exception {
    TaskInfo taskInfo = getHttpDownInfo().getTaskInfo();
    String filePath = taskInfo.buildTaskFilePath();
    FileUtil.deleteIfExists(filePath);
    long position = 0;
    taskInfo.setStatus(HttpDownStatus.MERGE);
    taskInfo.getChunkInfoList().forEach((chunk) -> chunk.setStatus(HttpDownStatus.MERGE));
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
    FileChannel fileChannel = new RandomAccessFile(
        getHttpDownInfo().getTaskInfo().buildChunkFilePath(chunkInfo.getIndex()), "rw")
        .getChannel();
    fileChannel.position(chunkInfo.getNowStartPosition());
    Closeable[] fileChannels = new Closeable[]{fileChannel};
    setAttr(chunkInfo, ATTR_FILE_CHANNELS, fileChannels);
    return fileChannels;
  }

  @Override
  public boolean doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    Closeable[] fileChannels = getFileWriter(chunkInfo);
    if (fileChannels != null && fileChannels.length > 0) {
      FileChannel fileChannel = (FileChannel) getFileWriter(chunkInfo)[0];
      if (fileChannel != null) {
        fileChannel.write(buffer);
        return true;
      }
    }
    return false;
  }
}
