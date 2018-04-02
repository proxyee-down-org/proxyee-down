package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
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
  public int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    int ret = buffer.remaining();
    MappedByteBuffer mappedByteBuffer = null;
    try (
        FileChannel fileChannel = new RandomAccessFile(
            getHttpDownInfo().getTaskInfo().buildTaskFilePath(), "rw").getChannel()
    ) {
      mappedByteBuffer = fileChannel
          .map(MapMode.READ_WRITE, chunkInfo.getOriStartPosition() + chunkInfo.getDownSize(), ret);
      mappedByteBuffer.put(buffer);
    } finally {
      if (mappedByteBuffer != null) {
        FileUtil.unmap(mappedByteBuffer);
      }
    }
    return ret;
  }
}
