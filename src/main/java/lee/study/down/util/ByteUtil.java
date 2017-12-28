package lee.study.down.util;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ByteUtil {

  /**
   * 大端序
   * @param num
   * @return
   */
  public static byte[] intToBtsForBig(int num){
    //int 4字节
    byte[] bts = new byte[4];
    for (int i = 0; i < bts.length; i++) {
      bts[bts.length-i-1] = (byte)((num >> 8*i)&0xFF);
    }
    return bts;
  }

  /**
   * 大端序
   * @param bts
   * @return
   */
  public static int btsToIntForBig(byte[] bts){
    //int 4字节
    int num = 0;
    for (int i = 0; i < bts.length; i++) {
      num += (bts[i]&0xFF) << 8*(bts.length-i-1);
    }
    return num;
  }

  /**
   * 小端序
   * @param num
   * @return
   */
  public static byte[] intToBtsForSmall(int num){
    //int 4字节
    byte[] bts = new byte[4];
    for (int i = 0; i < bts.length; i++) {
      bts[i] = (byte)((num >> 8*i)&0xFF);
    }
    return bts;
  }

  /**
   * 小端序
   * @param bts
   * @return
   */
  public static int btsToIntForSmall(byte[] bts){
    //int 4字节
    int num = 0;
    for (int i = 0; i < bts.length; i++) {
      num += (bts[i]&0xFF) << 8*i;
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

  public static byte[] hexToBts(String hex){
    byte[] bts = new byte[hex.length()/2];
    for (int i = 0; i < bts.length; i++) {
      bts[i] = (byte) Integer.parseInt(hex.substring(i*2,i*2+2),16);
    }
    return bts;
  }

  public static String btsToHex(byte[] bts){
    StringBuilder str = new StringBuilder();
    for (byte b : bts) {
      str.append(String.format("%2s",Integer.toHexString(b&0xFF)).replace(" ","0"));
    }
    return str.toString();
  }

  public static void main(String[] args) throws Exception {
    /*CRC32 crc32 = new CRC32();
    FileInputStream fileInputStream = new FileInputStream("F:\\down\\test1.txt");
    byte[] bts = new byte[1000];
    int len = fileInputStream.read(bts);
    crc32.update(bts,0,len);
    System.out.println(Long.toHexString(crc32.getValue()));*/

    FileInputStream inputStream = new FileInputStream("F:\\百度云合并下载研究\\百度云合并下载2.zip");
    inputStream.read(new byte[18]);
    byte[] bts4 = new byte[4];
    byte[] bts2 = new byte[2];
    inputStream.read(bts4);
    int fileSize = ByteUtil.btsToIntForSmall(bts4);
    System.out.println(fileSize);
    inputStream.read(bts4);
    System.out.println(ByteUtil.btsToIntForSmall(bts4));
    inputStream.read(bts2);
    int nameLength = ByteUtil.btsToIntForSmall(bts2);
    System.out.println(nameLength);
    inputStream.read(bts2);
    System.out.println(ByteUtil.btsToIntForSmall(bts2));
    byte[] nameBts = new byte[nameLength];
    inputStream.read(nameBts);
    System.out.println(new String(nameBts));
    byte[] fileBts = new byte[fileSize];
    inputStream.read(fileBts);
    System.out.println(ByteUtil.btsToHex(fileBts));

    inputStream.read(new byte[18]);
    inputStream.read(bts4);
    fileSize = ByteUtil.btsToIntForSmall(bts4);
    System.out.println(fileSize);
    inputStream.read(bts4);
    System.out.println(ByteUtil.btsToIntForSmall(bts4));
    inputStream.read(bts2);
    nameLength = ByteUtil.btsToIntForSmall(bts2);
    System.out.println(nameLength);
    inputStream.read(bts2);
    System.out.println(ByteUtil.btsToIntForSmall(bts2));
    nameBts = new byte[nameLength];
    inputStream.read(nameBts);
    System.out.println(new String(nameBts));
    fileBts = new byte[fileSize];
    inputStream.read(fileBts);
    System.out.println(ByteUtil.btsToHex(fileBts));

    inputStream.read(new byte[18]);
    inputStream.read(bts4);
    fileSize = ByteUtil.btsToIntForSmall(bts4);
    System.out.println(fileSize);
    inputStream.read(bts4);
    System.out.println(ByteUtil.btsToIntForSmall(bts4));
    inputStream.read(bts2);
    nameLength = ByteUtil.btsToIntForSmall(bts2);
    System.out.println(nameLength);
    inputStream.read(bts2);
    System.out.println(ByteUtil.btsToIntForSmall(bts2));
    nameBts = new byte[nameLength];
    inputStream.read(nameBts);
    System.out.println(new String(nameBts));
    fileBts = new byte[fileSize];
    inputStream.read(fileBts);
    System.out.println(ByteUtil.btsToHex(fileBts));

    inputStream.read(new byte[18]);
    inputStream.read(bts4);
    fileSize = ByteUtil.btsToIntForSmall(bts4);
    System.out.println(fileSize);
    inputStream.read(bts4);
    System.out.println(ByteUtil.btsToIntForSmall(bts4));
    inputStream.read(bts2);
    nameLength = ByteUtil.btsToIntForSmall(bts2);
    System.out.println(nameLength);
    inputStream.read(bts2);
    System.out.println(ByteUtil.btsToIntForSmall(bts2));
    nameBts = new byte[nameLength];
    inputStream.read(nameBts);
    System.out.println(new String(nameBts));
    fileBts = new byte[fileSize];
    inputStream.read(fileBts);
    System.out.println(ByteUtil.btsToHex(fileBts));
  }

}
