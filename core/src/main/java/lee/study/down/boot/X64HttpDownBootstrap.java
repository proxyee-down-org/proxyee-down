package lee.study.down.boot;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.io.LargeMappedByteBuffer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;

public class X64HttpDownBootstrap extends AbstractHttpDownBootstrap {

  public X64HttpDownBootstrap(HttpDownInfo httpDownInfo,
      int retryCount,
      SslContext clientSslContext,
      NioEventLoopGroup clientLoopGroup,
      HttpDownCallback callback,
      TimeoutCheckTask timeoutCheckTask) {
    super(httpDownInfo, retryCount, clientSslContext, clientLoopGroup, callback, timeoutCheckTask);
  }

  @Override
  public Closeable initFileWriter(ChunkInfo chunkInfo) throws Exception {
    Closeable closeable;
    if (getHttpDownInfo().getTaskInfo().getConnections() > 1) {
      closeable = new LargeMappedByteBuffer(getHttpDownInfo().getTaskInfo().buildTaskFilePath(),
          chunkInfo.getNowStartPosition(),
          chunkInfo.getEndPosition() - chunkInfo.getNowStartPosition() + 1);
    } else {
      closeable = new RandomAccessFile(getHttpDownInfo().getTaskInfo().buildTaskFilePath(), "rw")
          .getChannel();
    }
    setAttr(chunkInfo, ATTR_FILE_CLOSEABLE, closeable);
    return closeable;
  }

  @Override
  public int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    int ret = -1;
    Closeable closeable = (Closeable) getAttr(chunkInfo, ATTR_FILE_CLOSEABLE);
    if (closeable != null) {
      ret = buffer.remaining();
      if(closeable instanceof LargeMappedByteBuffer){
        LargeMappedByteBuffer largeMappedByteBuffer = (LargeMappedByteBuffer) closeable;
        largeMappedByteBuffer.put(buffer);
      }else{
        FileChannel fileChannel = (FileChannel) closeable;
        fileChannel.write(buffer);
      }
    }
    return ret;
  }

}
