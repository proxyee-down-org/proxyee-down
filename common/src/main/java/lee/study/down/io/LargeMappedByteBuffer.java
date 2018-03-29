package lee.study.down.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.List;

public class LargeMappedByteBuffer implements Closeable {

  private FileChannel fileChannel;
  private List<MappedByteBuffer> bufferList;
  private long rawPosition;
  private long position;
  private long size;

  public LargeMappedByteBuffer(String path, long position, long size)
      throws IOException {
    fileChannel = new RandomAccessFile(path, "rw").getChannel();
    this.rawPosition = position;
    this.position = position;
    this.size = size;
    int count = (int) Math.ceil(size / (double) Integer.MAX_VALUE);
    this.bufferList = new LinkedList<>();
    long calcPos = position;
    for (int i = 0; i < count; i++) {
      long calcSize = i + 1 == count ? size % Integer.MAX_VALUE : Integer.MAX_VALUE;
      bufferList.add(fileChannel.map(MapMode.READ_WRITE, calcPos, calcSize));
      calcPos += calcSize;
    }
  }

  public final void put(ByteBuffer byteBuffer) throws IOException {
    try {
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
    } catch (Exception e) {
      throw new IOException(
          "LargeMappedByteBuffer put rawPosition-" + rawPosition + "\tposition-" + position
              + "\tsize-" + size, e);
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
      fileChannel.close();
    } catch (Exception e) {
      throw new IOException("LargeMappedByteBuffer close", e);
    }
  }

}
