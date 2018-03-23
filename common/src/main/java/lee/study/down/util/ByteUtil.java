package lee.study.down.util;

import io.netty.buffer.ByteBuf;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

public class ByteUtil {

  /**
   * 大端序
   */
  public static byte[] numToBtsForBig(long num, int len) {
    byte[] bts = new byte[len];
    for (int i = 0; i < bts.length; i++) {
      bts[bts.length - i - 1] = (byte) ((num >> 8 * i) & 0xFF);
    }
    return bts;
  }

  /**
   * 大端序
   */
  public static byte[] numToBtsForBig(long num) {
    //long 8字节
    return numToBtsForBig(num, 8);
  }

  /**
   * 大端序
   */
  public static long btsToNumForBig(byte[] bts) {
    //int 4字节
    long num = 0;
    for (int i = 0; i < bts.length; i++) {
      num += ((long) (bts[i] & 0xFF)) << 8 * (bts.length - i - 1);
    }
    return num;
  }

  /**
   * 小端序
   */
  public static byte[] numToBtsForSmall(long num, int len) {
    byte[] bts = new byte[len];
    for (int i = 0; i < bts.length; i++) {
      bts[i] = (byte) ((num >> 8 * i) & 0xFF);
    }
    return bts;
  }

  /**
   * 小端序
   */
  public static byte[] numToBtsForSmall(long num) {
    return numToBtsForSmall(num, 8);
  }

  /**
   * 小端序
   */
  public static long btsToNumForSmall(byte[] bts) {
    //int 4字节
    long num = 0;
    for (int i = 0; i < bts.length; i++) {
      num += ((long) (bts[i] & 0xFF)) << 8 * i;
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

  public static void serialize(Serializable object, String path, boolean isHidden)
      throws IOException {
    serialize(object, path, null, isHidden);
  }

  public static void serialize(Serializable object, String path, String bakPath, boolean isHidden)
      throws IOException {
    FileUtil.initFile(path, isHidden);
    try (
        RandomAccessFile raf = new RandomAccessFile(path, "rw")
    ) {
      raf.write(objToBts(object));
    }
    if (bakPath != null) {
      FileUtil.initFile(bakPath, isHidden);
      try (
          RandomAccessFile raf2 = new RandomAccessFile(bakPath, "rw")
      ) {
        raf2.write(objToBts(object));
      }
    }
  }

  public static void serialize(Serializable object, String path, String bakPath)
      throws IOException {
    serialize(object, path, bakPath, true);
  }

  public static void serialize(Serializable object, String path)
      throws IOException {
    serialize(object, path, true);
  }

  public static Object deserialize(String path) throws IOException, ClassNotFoundException {
    try (
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))
    ) {
      return ois.readObject();
    }
  }

  public static Object deserialize(String path, String bakPath)
      throws IOException, ClassNotFoundException {
    try (
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))
    ) {
      return ois.readObject();
    } catch (Exception e) {
      try (
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(bakPath))
      ) {
        return ois.readObject();
      }
    }
  }

  public static int findText(ByteBuf byteBuf, String str) {
    byte[] text = str.getBytes();
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
    return insertText(byteBuf, index, str, Charset.defaultCharset());
  }

  public static ByteBuf insertText(ByteBuf byteBuf, int index, String str, Charset charset) {
    byte[] begin = new byte[index + 1];
    byte[] end = new byte[byteBuf.readableBytes() - begin.length];
    byteBuf.readBytes(begin);
    byteBuf.readBytes(end);
    byteBuf.writeBytes(begin);
    byteBuf.writeBytes(str.getBytes(charset));
    byteBuf.writeBytes(end);
    return byteBuf;
  }

  public static byte[] hexToBts(String hex) {
    byte[] bts = new byte[hex.length() / 2];
    for (int i = 0; i < bts.length; i++) {
      bts[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
    }
    return bts;
  }

  public static String btsToHex(byte[] bts) {
    StringBuilder str = new StringBuilder();
    for (byte b : bts) {
      str.append(String.format("%2s", Integer.toHexString(b & 0xFF)).replace(" ", "0"));
    }
    return str.toString();
  }

  public static String readContent(InputStream inputStream, Charset charset) {
    StringBuilder sb = new StringBuilder();
    try (
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line + System.lineSeparator());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  public static String readContent(InputStream inputStream) {
    return readContent(inputStream, Charset.forName("UTF-8"));
  }

  public static String readJsContent(InputStream inputStream) {
    return readJsContent(inputStream, Charset.defaultCharset());
  }

  public static String readJsContent(InputStream inputStream, Charset charset) {
    StringBuilder sb = new StringBuilder(readContent(inputStream, charset));
    sb.insert(0, "<script type=\"text/javascript\">");
    sb.append("</script>");
    return sb.toString();
  }

  /**
   * 查找buffer中一段字节数的位置
   *
   * @param buffer 待查找的buffer
   * @param btsArr 多个为或的关系
   */
  public static int findBytes(ByteBuffer buffer, byte[]... btsArr) {
    int[] indexArray = new int[btsArr.length];
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      for (int i = 0; i < btsArr.length; i++) {
        if (indexArray[i] == -1) {
          indexArray[i] = 0;
        }
        byte[] bts = btsArr[i];
        if (b == bts[indexArray[i]]) {
          indexArray[i]++;
          if (indexArray[i] == bts.length) {
            return buffer.position() - bts.length;
          }
        } else {
          indexArray[i] = 0;
        }
      }
    }
    return -1;
  }

  public static long getNextTokenSize(FileChannel fileChannel, long position, byte[]... btsArr)
      throws IOException {
    return getNextTokenSize(fileChannel, -1, position, btsArr);
  }

  public static long getNextTokenSize(FileChannel fileChannel, long start, long position,
      byte[]... btsArr)
      throws IOException {
    long ret = -1;
    ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    long startPosition;
    if (start >= 0) {
      startPosition = start;
    } else {
      startPosition = fileChannel.position();
    }
    if (position >= 0) {
      fileChannel.position(position);
    }
    outer:
    while (fileChannel.read(buffer) != -1) {
      buffer.flip();
      int index;
      while ((index = findBytes(buffer, btsArr)) != -1) {
        ret = fileChannel.position() - startPosition - buffer.limit() + index;
        break outer;
      }
      buffer.clear();
    }
    fileChannel.position(startPosition);
    return ret;
  }

  public static boolean matchToken(FileChannel fileChannel, long position, byte[] bts)
      throws IOException {
    return matchToken(fileChannel, -1, position, bts);
  }

  public static boolean matchToken(FileChannel fileChannel, long start, long position,
      byte[]... btsArr)
      throws IOException {
    boolean ret;
    ByteBuffer buffer = ByteBuffer.allocate(btsArr[0].length);
    long rawPosition;
    if (start >= 0) {
      rawPosition = start;
    } else {
      rawPosition = fileChannel.position();
    }
    if (position >= 0) {
      fileChannel.position(position);
    }
    fileChannel.read(buffer);
    buffer.flip();
    ret = findBytes(buffer, btsArr) == 0;
    fileChannel.position(rawPosition);
    return ret;
  }

  public static String getCertHash(X509Certificate certificate) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] der = certificate.getEncoded();
    md.update(der);
    return ByteUtil.btsToHex(md.digest());
  }
}
