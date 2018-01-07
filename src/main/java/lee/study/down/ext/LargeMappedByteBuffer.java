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

public class LargeMappedByteBuffer implements Closeable {

  private List<MappedByteBuffer> bufferList;
  private long rawPosition;
  private long position;
  private long size;

  public LargeMappedByteBuffer(FileChannel fileChannel, MapMode mapMode, long position, long size)
      throws IOException {
    this.rawPosition = position;
    this.position = position;
    this.size = size;
    int count = (int) Math.ceil(size / (double) Integer.MAX_VALUE);
    this.bufferList = new LinkedList<>();
    long calcPos = position;
    for (int i = 0; i < count; i++) {
      long calcSize = i + 1 == count ? size % Integer.MAX_VALUE : Integer.MAX_VALUE;
      bufferList.add(fileChannel.map(mapMode, calcPos, calcSize));
      calcPos += calcSize;
    }
  }

  public final void put(ByteBuffer byteBuffer) throws IOException {
    try {
      int index = getIndex();
      long byteBufferRemaining = byteBuffer.remaining();
      this.position += byteBufferRemaining;
      MappedByteBuffer mappedBuffer = bufferList.get(index);
      long mapBufferRemaining = mappedBuffer.remaining();
      if (mapBufferRemaining < byteBufferRemaining) {
        if (index + 1 > bufferList.size() - 1) {
          throw new IOException(
              "size error byteBufferRemaining-" + byteBufferRemaining + " mapBufferRemaining-"
                  + mapBufferRemaining);
        }
        byte[] temp = new byte[(int) mapBufferRemaining];
        byteBuffer.get(temp);
        bufferList.get(index).put(temp);
        bufferList.get(index + 1).put(byteBuffer);
      } else {
        bufferList.get(index).put(byteBuffer);
      }
    } catch (Exception e) {
      throw new IOException(
          "LargeMappedByteBuffer put position-" + position + " rawPosition-" + rawPosition
              + " size-" + size, e);
    }
  }

  private int getIndex() {
    return (int) ((this.position - this.rawPosition) / Integer.MAX_VALUE);
  }

  @Override
  public void close() throws IOException {
    try {
      Class<?> clazz = Class.forName("sun.nio.ch.FileChannelImpl");
      Method m = clazz.getDeclaredMethod("unmap", MappedByteBuffer.class);
      m.setAccessible(true);
      for (MappedByteBuffer mappedBuffer : bufferList) {
        m.invoke(clazz, mappedBuffer);
      }
    } catch (Exception e) {
      throw new IOException("LargeMappedByteBuffer close", e);
    }
  }
}
