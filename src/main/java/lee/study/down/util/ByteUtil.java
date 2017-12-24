package lee.study.down.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class ByteUtil {

  /**
   * 大端序
   */
  public static byte[] intToBts(int num) {
    //int 4字节
    byte[] bts = new byte[4];
    for (int i = 0; i < bts.length; i++) {
      bts[bts.length - i - 1] = (byte) ((num >> 8 * i) & 0xFF);
    }
    return bts;
  }

  /**
   * 大端序
   */
  public static int btsToInt(byte[] bts) {
    //int 4字节
    int num = 0;
    for (int i = 0; i < bts.length; i++) {
      num += (bts[i] & 0xFF) << 8 * (bts.length - i - 1);
    }
    return num;
  }

  public static byte[] objToBts(Serializable object) throws IOException {
    try (
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
    ) {
      ObjectOutputStream outputStream = new ObjectOutputStream(baos);
      outputStream.writeObject(object);
      return baos.toByteArray();
    }
  }

  /**
   * 对象序列化至文件中 head 4byte body
   */
  public static void appendObject(Serializable object, String path) throws IOException {
    File file = new File(path);
    if (!file.exists()) {
      file.createNewFile();
    }
    try (
        FileOutputStream outputStream = new FileOutputStream(file, true)
    ) {
      byte[] body = objToBts(object);
      //body长度
      outputStream.write(intToBts(body.length));
      outputStream.write(body);
    }
  }

  public static void serialize(Serializable object, String path) throws IOException {
    FileUtil.createFile(path);
    try (
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))
    ) {
      outputStream.writeObject(object);
    }
  }

  public static Object deserialize(byte[] bts) throws IOException, ClassNotFoundException {
    try (
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bts))
    ) {
      return ois.readObject();
    }
  }

  public static Object deserialize(String path) throws IOException, ClassNotFoundException {
    try (
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))
    ) {
      return ois.readObject();
    }
  }

  public static byte[] stringToBytes(String str) {
    byte[] bts = new byte[str.length()];
    for (int i = 0; i < str.length(); i++) {
      bts[i] = (byte) str.charAt(i);
    }
    return bts;
  }

  public static int findText(ByteBuf byteBuf, String str) {
    byte[] text = stringToBytes(str);
    int matchIndex = 0;
    for (int i = byteBuf.readerIndex(); i < byteBuf.readableBytes(); i++) {
      for (int j = matchIndex; j < text.length; j++) {
        if (byteBuf.getByte(i) == text[j]) {
          matchIndex = j + 1;
          if (matchIndex == text.length) {
            return i;
          }
        } else {
          matchIndex = 0;
        }
        break;
      }
    }
    return -1;
  }

  public static ByteBuf insertText(ByteBuf byteBuf, int index, String str) {
    byte[] begin = new byte[index + 1];
    byte[] end = new byte[byteBuf.readableBytes() - begin.length];
    byteBuf.readBytes(begin);
    byteBuf.readBytes(end);
    byteBuf.writeBytes(begin);
    byteBuf.writeBytes(stringToBytes(str));
    byteBuf.writeBytes(end);
    return byteBuf;
  }

  public static void main(String[] args) {
    ByteBuf byteBuf = Unpooled.buffer();
    byteBuf.writeByte('a');
    byteBuf.writeByte('b');
    byteBuf.writeByte('c');
    byteBuf.writeByte('d');
    System.out.println(findText(byteBuf, "c"));
    System.out.println(
        insertText(byteBuf, -1, "test").toString(Charset.defaultCharset()));
  }

}
