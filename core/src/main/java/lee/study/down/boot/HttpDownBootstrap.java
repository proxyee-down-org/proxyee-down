package lee.study.down.boot;

import io.netty.handler.ssl.SslContext;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;

public class HttpDownBootstrap extends AbstractHttpDownBootstrap {


  public HttpDownBootstrap(HttpDownInfo httpDownInfo, int retryCount, SslContext clientSslContext, HttpDownCallback callback, TimeoutCheckTask timeoutCheckTask) {
    super(httpDownInfo, retryCount, clientSslContext, callback, timeoutCheckTask);
  }

  @Override
  public int doFileWriter(ChunkInfo chunkInfo, ByteBuffer buffer) throws IOException {
    int ret = -1;
    Closeable closeable = (Closeable) getAttr(chunkInfo, ATTR_FILE_CLOSEABLE);
    if (closeable != null) {
      ret = buffer.remaining();
      SeekableByteChannel fileChannel = (SeekableByteChannel) closeable;
      fileChannel.write(buffer);
    }
    return ret;
  }

  @Override
  public Closeable initFileWriter(ChunkInfo chunkInfo) throws Exception {
    SeekableByteChannel fileChannel = Files.newByteChannel(Paths.get(getHttpDownInfo().getTaskInfo().buildTaskFilePath()), StandardOpenOption.WRITE);
    fileChannel.position(chunkInfo.getOriStartPosition() + chunkInfo.getDownSize());
    setAttr(chunkInfo, ATTR_FILE_CLOSEABLE, fileChannel);
    return fileChannel;
  }
}
