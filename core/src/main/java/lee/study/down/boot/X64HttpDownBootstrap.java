package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.io.Mmap;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;

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
  public void afterStart() throws Exception {
    mmap = new Mmap(getHttpDownInfo().getTaskInfo().buildTaskFilePath(),
        getHttpDownInfo().getTaskInfo().getTotalSize());
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
