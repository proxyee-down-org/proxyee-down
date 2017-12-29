package lee.study.down.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.var;

public class ByteUtil {

  /**
   * 大端序
   */
  public static byte[] longToBtsForBig(long num) {
    //int 4字节
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
  public static byte[] longToBtsForSmall(long num) {
    //int 4字节
    byte[] bts = new byte[8];
    for (int i = 0; i < bts.length; i++) {
      bts[i] = (byte) ((num >> 8 * i) & 0xFF);
    }
    return bts;
  }

  /**
   * 小端序
   */
  public static long btsToLongForSmall(byte[] bts) {
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

  private static void readEntry(InputStream inputStream) throws IOException {
    inputStream.read(new byte[18]);
    byte[] bts4 = new byte[4];
    byte[] bts2 = new byte[2];
    byte[] bts8 = new byte[8];
    inputStream.read(bts4);
    System.out.println("压缩前：" + btsToLongForSmall(bts4));
    inputStream.read(bts4);
    long fileSize = btsToLongForSmall(bts4);
    System.out.println("压缩后：" + fileSize);
    inputStream.read(bts2);
    long nameLength = btsToLongForSmall(bts2);
    System.out.println("文件名长度：" + nameLength);
    inputStream.read(bts2);
    long extLength = btsToLongForSmall(bts2);
    System.out.println("扩展长度：" + extLength);
    byte[] nameBts = new byte[(int) nameLength];
    inputStream.read(nameBts);
    System.out.println("文件名:" + new String(nameBts, "gbk"));
    if (extLength > 0) {
      inputStream.skip(4);
      inputStream.read(bts8);
      System.out.println("扩展压缩前：" + btsToLongForSmall(bts8));
      inputStream.read(bts8);
      System.out.println("扩展压缩后：" + btsToLongForSmall(bts8));
      inputStream.skip(fileSize);
    }
    System.out.println("跳过文件长度：" + fileSize);
    /*byte[] fileBts = new byte[(int) fileSize];
    inputStream.read(fileBts);
    System.out.println("文件内容："+ByteUtil.btsToHex(fileBts));*/
  }

  public static void main(String[] args) throws Exception {
    /*CRC32 crc32 = new CRC32();
    FileInputStream fileInputStream = new FileInputStream("F:\\down\\test1.txt");
    byte[] bts = new byte[1000];
    int len = fileInputStream.read(bts);
    crc32.update(bts,0,len);
    System.out.println(Long.toHexString(crc32.getValue()));*/
    /*long num = btsToLongForSmall(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    System.out.println(Long.valueOf("FF",16));
    System.out.println(Integer.toHexString(255));
    System.out.println(btsToLongForSmall(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
    System.out.println(btsToHex(longToBtsForSmall(4294967295L)));*/
    //System.out.println(btsToHex(longToBtsForBig(4294967296L)));
    FileChannel fileChannel = new FileInputStream("f:/down/鬼子来了.zip").getChannel();
    while(true){
      BdyZipEntry zipEntry = getNextBdyZipEntry(fileChannel);
      System.out.println(btsToHex(zipEntry.getHeader()));
      System.out.println(zipEntry.getFileName());
      System.out.println(btsToHex(zipEntry.getCrc32()));
      System.out.println(zipEntry.getCompressedSize());
      System.out.println(getNextTokenSize(fileChannel,ZIP_ENTRY_FILE_HEARD,ZIP_ENTRY_DIR_HEARD));
    }
    /*FileChannel fileChannel = new FileInputStream("f:/down/鬼子来了.zip").getChannel();
    fileChannel.position();
    fileChannel.read(ByteBuffer.allocate(5));
    System.out.println(fileChannel.position());*/
//    scannerBdyZipEntry(fileChannel);
    /*ByteBuffer buffer = ByteBuffer.allocate(20);
    buffer.put(new byte[]{0, 0, 0, 0});
    buffer.put(ZIP_ENTRY_DIR_HEARD);
    buffer.flip();
    byte[] bts = new byte[4];
    buffer.get(bts);
    System.out.println(btsToHex(bts));*/
//    System.out.println(findBytes(buffer, ZIP_ENTRY_FILE_HEARD));
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
    zipEntry.setCompressedSize(btsToLongForSmall(bts4));
    buffer.get(bts4);
    zipEntry.setUnCompressedSize(btsToLongForSmall(bts4));
    byte[] bts2 = new byte[2];
    buffer.get(bts2);
    zipEntry.setFileNameLength(btsToLongForSmall(bts2));
    buffer.get(bts2);
    zipEntry.setExtraFieldLength(btsToLongForSmall(bts2));

    ByteBuffer fileNameBuffer = ByteBuffer.allocate((int) zipEntry.getFileNameLength());
    fileChannel.read(fileNameBuffer);
    fileNameBuffer.flip();
    zipEntry.setFileName(Charset.forName("GBK").decode(fileNameBuffer).toString());
    if (zipEntry.getExtraFieldLength() > 0) {
      ByteBuffer extraFieldBuffer = ByteBuffer.allocate((int) zipEntry.getExtraFieldLength());
      fileChannel.read(fileNameBuffer);
      zipEntry.setExtraField(extraFieldBuffer.array());
    }
    return zipEntry;
  }

/*  public static void scannerBdyZipEntry(FileChannel fileChannel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    while (fileChannel.read(buffer) != -1) {
      buffer.flip();
      int index;
      while ((index = findBytes(buffer, ZIP_ENTRY_HEARD)) != -1) {
        int postion =
      }
    }
    System.out.println(buffer.position());
  }*/

  private static final byte[] ZIP_ENTRY_FILE_HEARD = new byte[]{0x50, 0x4B, 0x03, 0x04};
  private static final byte[] ZIP_ENTRY_DIR_HEARD = new byte[]{0x50, 0x4B, 0x01, 0x02};

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
    ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    if (position >= 0) {
      fileChannel.position(position);
    }
    while (fileChannel.read(buffer) != -1) {
      buffer.flip();
      int index;
      while ((index = findBytes(buffer, btsArr)) != -1) {
        return fileChannel.position() - (position > 0 ? position : 0) - index;
      }
    }
    return -1;
  }

}
