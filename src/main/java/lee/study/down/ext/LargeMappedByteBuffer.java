package lee.study.down.ext;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.FileChannelImpl;

public class LargeMappedByteBuffer implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(LargeMappedByteBuffer.class);

  private List<MappedByteBuffer> bufferList;
  private long rawPosition;
  private long position;

  public LargeMappedByteBuffer(FileChannel fileChannel, MapMode mapMode, long position, long size)
      throws IOException {
    this.rawPosition = position;
    this.position = position;
    int count = (int) Math.ceil(size / (double) Integer.MAX_VALUE);
    this.bufferList = new LinkedList<>();
    long calcPos = position;
    for (int i = 0; i < count; i++) {
      long calcSize = i + 1 == count ? size % Integer.MAX_VALUE : Integer.MAX_VALUE;
      bufferList.add(fileChannel.map(mapMode, calcPos, calcSize));
      calcPos += calcSize;
    }
  }

  public final void put(ByteBuffer byteBuffer) {
    int index = getIndex();
    long length = byteBuffer.limit() - byteBuffer.position();
    this.position += length;
    MappedByteBuffer mappedBuffer = bufferList.get(index);
    if (mappedBuffer.remaining() < length) {
      byte[] temp = new byte[mappedBuffer.remaining()];
      byteBuffer.get(temp);
      bufferList.get(index).put(temp);
      bufferList.get(index + 1).put(byteBuffer);
    } else {
      bufferList.get(index).put(byteBuffer);
    }
  }

  private int getIndex() {
    return (int) ((this.position - this.rawPosition) / Integer.MAX_VALUE);
  }

  @Override
  public void close() throws IOException {
    try {
      Method m = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
      m.setAccessible(true);
      for (MappedByteBuffer mappedBuffer : bufferList) {
        m.invoke(FileChannelImpl.class, mappedBuffer);
      }
    } catch (Exception e) {
      LOGGER.error("LargeMappedByteBuffer close:",e);
    }
  }

}
