package lee.study.down.util;

import io.netty.buffer.ByteBuf;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import lee.study.down.HttpDownServer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public class ByteUtil {

  /**
   * 大端序
   */
  public static byte[] longToBtsForBig(long num) {
    //long 8字节
    byte[] bts = new byte[8];
    for (int i = 0; i < bts.length; i++) {
      bts[bts.length - i - 1] = (byte) ((num >> 8 * i) & 0xFF);
    }
    return bts;
  }

  /**
   * 大端序
   */
  public static long btsToLongForBig(byte[] bts) {
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

  public static void serialize(Serializable object, String path) throws IOException {
    FileUtil.createFile(path);
    try (
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))
    ) {
      outputStream.writeObject(object);
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

  public static String readJsContent(InputStream inputStream) {
    StringBuilder sb = new StringBuilder();
    try (
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    ) {
      sb.append("<script type=\"text/javascript\">");
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      sb.append("</script>");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  public static void insertContent(String path, long offset, byte[] content) throws IOException {
    RandomAccessFile sourceRaf = new RandomAccessFile(path, "rw");
    File tempFile = FileUtil.createFile(path + ".tmp");
    RandomAccessFile tempRaf = new RandomAccessFile(tempFile, "rw");
    long fileSize = sourceRaf.length();
    FileChannel sourceChannel = sourceRaf.getChannel();
    FileChannel targetChannel = tempRaf.getChannel();
    long remaining = fileSize - offset;
    long position = offset;
    while (remaining > 0) {
      long transferred = sourceChannel.transferTo(position, remaining, targetChannel);
      remaining -= transferred;
      position += transferred;
    }
    sourceChannel.truncate(offset);
    sourceRaf.seek(offset);
    sourceRaf.write(content);
    long newOffset = sourceRaf.getFilePointer();
    targetChannel.position(0);
    sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
    sourceChannel.close();
    targetChannel.close();
    FileUtil.deleteIfExists(tempFile);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Accessors(chain = true)
  public static class BdyZipEntry {

    private byte[] header = new byte[4];
    private byte[] version = new byte[2];
    private byte[] general = new byte[2];
    private byte[] method = new byte[2];
    private byte[] time = new byte[2];
    private byte[] date = new byte[2];
    private byte[] crc32 = new byte[4];
    private long compressedSize;
    private long unCompressedSize;
    private long fileNameLength;
    private long extraFieldLength;
    private String fileName;
    private byte[] extraField;
    private boolean isRepair;
  }

  public static BdyZipEntry getNextBdyZipEntry(FileChannel fileChannel) throws IOException {
    BdyZipEntry zipEntry = new BdyZipEntry();
    ByteBuffer buffer = ByteBuffer.allocate(30);
    fileChannel.read(buffer);
    buffer.flip();
    buffer.get(zipEntry.getHeader());
    buffer.get(zipEntry.getVersion());
    buffer.get(zipEntry.getGeneral());
    buffer.get(zipEntry.getMethod());
    buffer.get(zipEntry.getTime());
    buffer.get(zipEntry.getDate());
    buffer.get(zipEntry.getCrc32());

    byte[] bts4 = new byte[4];
    buffer.get(bts4);
    zipEntry.setCompressedSize(btsToNumForSmall(bts4));
    buffer.get(bts4);
    zipEntry.setUnCompressedSize(btsToNumForSmall(bts4));
    byte[] bts2 = new byte[2];
    buffer.get(bts2);
    zipEntry.setFileNameLength(btsToNumForSmall(bts2));
    buffer.get(bts2);
    zipEntry.setExtraFieldLength(btsToNumForSmall(bts2));

    ByteBuffer fileNameBuffer = ByteBuffer.allocate((int) zipEntry.getFileNameLength());
    fileChannel.read(fileNameBuffer);
    fileNameBuffer.flip();
    zipEntry.setFileName(Charset.forName("GBK").decode(fileNameBuffer).toString());
    if (zipEntry.getExtraFieldLength() > 0) {
      ByteBuffer extraFieldBuffer = ByteBuffer.allocate((int) zipEntry.getExtraFieldLength());
      fileChannel.read(extraFieldBuffer);
      zipEntry.setExtraField(extraFieldBuffer.array());
    }
    return zipEntry;
  }

  private static final byte[] ZIP_ENTRY_FILE_HEARD = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x0A, 0x00,
      0x00, 0x00, 0x00, 0x00};
  private static final byte[] ZIP_ENTRY_DIR_HEARD = new byte[]{0x50, 0x4B, 0x01, 0x02, 0x00, 0x00,
      0x0A, 0x00, 0x00, 0x00};

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

  public static long getNextTokenSize(FileChannel fileChannel, byte[]... btsArr)
      throws IOException {
    return getNextTokenSize(fileChannel, -1, btsArr);
  }

  public static long getNextTokenSize(FileChannel fileChannel, long position, byte[]... btsArr)
      throws IOException {
    long ret = -1;
    ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    if (position >= 0) {
      fileChannel.position(position);
    }
    long startPosition = fileChannel.position();
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
    boolean ret;
    ByteBuffer buffer = ByteBuffer.allocate(bts.length);
    long rawPosition = fileChannel.position();
    if (position >= 0) {
      fileChannel.position(position);
    }
    fileChannel.read(buffer);
    buffer.flip();
    ret = findBytes(buffer, bts) == 0;
    fileChannel.position(rawPosition);
    return ret;
  }

  public static void unzipBdy(String path, String toPath) throws IOException {
    File zipFile = new File(path);
    File toDir = FileUtil.createDirSmart(toPath);
    FileChannel fileChannel = new RandomAccessFile(zipFile, "rw").getChannel();
    boolean isEnd = false;
    while (!isEnd) {
      BdyZipEntry bdyZipEntry = getNextBdyZipEntry(fileChannel);
      long fileSize = bdyZipEntry.getCompressedSize();
      if (matchToken(fileChannel, fileChannel.position() + fileSize, ZIP_ENTRY_DIR_HEARD)) {
        isEnd = true;
      } else if (!matchToken(fileChannel, fileChannel.position() + fileSize,
          ZIP_ENTRY_FILE_HEARD)) {
        //找到真实文件长度
        fileSize = getNextTokenSize(fileChannel, ZIP_ENTRY_FILE_HEARD, ZIP_ENTRY_DIR_HEARD);
        if (matchToken(fileChannel, fileChannel.position() + fileSize, ZIP_ENTRY_DIR_HEARD)) {
          isEnd = true;
        }
      }
      HttpDownServer.LOGGER.debug("bdyUnzip:"+bdyZipEntry.getFileName()+"\t"+fileSize);
      if (fileSize == 0
          && bdyZipEntry.getFileName().lastIndexOf("/") == bdyZipEntry.getFileName().length() - 1) {
        FileUtil.createDirSmart(toDir.getPath() + File.separator + bdyZipEntry.getFileName());
      } else {
        File unzipFile = FileUtil
            .createFileSmart(toDir.getPath() + File.separator + bdyZipEntry.getFileName());
        FileChannel unzipChannel = new RandomAccessFile(unzipFile, "rw").getChannel();
        long position = fileChannel.position();
        long remaining = fileSize;
        while (remaining > 0) {
          long transferred = fileChannel.transferTo(position, remaining, unzipChannel);
          remaining -= transferred;
          position += transferred;
        }
        unzipChannel.close();
        fileChannel.position(fileChannel.position() + fileSize);
      }
      if (fileChannel.position() == fileChannel.size()) {
        isEnd = true;
      }
    }
    fileChannel.close();
  }

  public static void main(String[] args) throws Exception {
    unzipBdy("F:\\down\\鬼子来了.zip", "F:\\down\\");
  }

}
