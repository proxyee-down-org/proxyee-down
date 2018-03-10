package lee.study.down.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lombok.Data;

public class BdyZip {

  private static final byte[] ZIP_ENTRY_FILE_HEARD = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x0A,
      0x00, 0x00, 0x00, 0x00, 0x00};
  private static final byte[] ZIP_ENTRY_DIR_HEARD = new byte[]{0x50, 0x4B, 0x01, 0x02, 0x00,
      0x00, 0x0A, 0x00, 0x00, 0x00};
  private static final long _4G = 1024 * 1024 * 1024 * 4L - 1;
  private static final long _512M = 1024 * 1024 * 521L;

  @Data
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
    private boolean isDir;
    private long fileStartPosition;
    private boolean isEnd;
  }

  public static BdyZipEntry getNextBdyZipEntry(FileChannel fileChannel, long position)
      throws IOException {
    if (position > 0) {
      fileChannel.position(position);
    }
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
    zipEntry.setCompressedSize(ByteUtil.btsToNumForSmall(bts4));
    buffer.get(bts4);
    zipEntry.setUnCompressedSize(ByteUtil.btsToNumForSmall(bts4));
    byte[] bts2 = new byte[2];
    buffer.get(bts2);
    zipEntry.setFileNameLength(ByteUtil.btsToNumForSmall(bts2));
    buffer.get(bts2);
    zipEntry.setExtraFieldLength(ByteUtil.btsToNumForSmall(bts2));

    ByteBuffer fileNameBuffer = ByteBuffer.allocate((int) zipEntry.getFileNameLength());
    fileChannel.read(fileNameBuffer);
    fileNameBuffer.flip();
    zipEntry.setFileName(Charset.forName("GB18030").decode(fileNameBuffer).toString());
    if (zipEntry.getExtraFieldLength() > 0) {
      fileChannel.position(fileChannel.position() + zipEntry.getExtraFieldLength());
    }
    if (zipEntry.getCompressedSize() == 0
        && (zipEntry.getFileName().length() == 0
        || "/".equals(zipEntry.getFileName().substring(zipEntry.getFileName().length() - 1)))) {
      zipEntry.setDir(true);
    }
    zipEntry.setFileStartPosition(fileChannel.position());
    return zipEntry;
  }

  public static BdyZipEntry getNextBdyZipEntry(FileChannel fileChannel)
      throws IOException {
    return getNextBdyZipEntry(fileChannel, -1);
  }

  public static BdyZipEntry getNextFixedBdyZipEntry(FileChannel fileChannel,
      List<String> centralList,
      BdyUnzipCallback callback)
      throws IOException {
    BdyZipEntry zipEntry = getNextBdyZipEntry(fileChannel);
    boolean fixFlag = false;
    if (ByteUtil
        .matchToken(fileChannel,
            zipEntry.getFileStartPosition(),
            zipEntry.getFileStartPosition() + zipEntry.getCompressedSize(),
            ZIP_ENTRY_DIR_HEARD)) {
      zipEntry.setEnd(true);
    } else if (!ByteUtil
        .matchToken(fileChannel,
            zipEntry.getFileStartPosition(),
            zipEntry.getFileStartPosition() + zipEntry.getCompressedSize(),
            ZIP_ENTRY_FILE_HEARD)) {
      long fixedSize = fixedEntrySize(fileChannel, zipEntry, centralList, callback);
      if (fixedSize == -1) {
        throw new RuntimeException("修复失败,文件损坏请重新下载");
      }
      zipEntry.setUnCompressedSize(fixedSize);
      zipEntry.setCompressedSize(fixedSize);
      if (ByteUtil
          .matchToken(fileChannel, zipEntry.getFileStartPosition() + zipEntry.getCompressedSize(),
              ZIP_ENTRY_DIR_HEARD)) {
        zipEntry.setEnd(true);
        fixFlag = true;
      }
    }
    if (!zipEntry.isEnd()) {
      fileChannel.position(zipEntry.getFileStartPosition() + zipEntry.getCompressedSize());
    }
    if (callback != null && !fixFlag) {
      callback.onFix(fileChannel.size(),
          zipEntry.getFileStartPosition() + zipEntry.getCompressedSize());
    }
    return zipEntry;
  }

  private static long fixedEntrySize(FileChannel fileChannel, BdyZipEntry fixEntry,
      List<String> centralList, BdyUnzipCallback callback)
      throws IOException {
    long skipSize = _4G;
    if (callback != null) {
      callback.onFix(fileChannel.size(), fixEntry.getFileStartPosition() + skipSize);
    }
    long fixedSize = ByteUtil
        .getNextTokenSize(fileChannel, fixEntry.getFileStartPosition(),
            fixEntry.getFileStartPosition() + skipSize,
            ZIP_ENTRY_FILE_HEARD, ZIP_ENTRY_DIR_HEARD);
    BdyZipEntry nextEntry = getNextBdyZipEntry(fileChannel,
        fixEntry.getFileStartPosition() + fixedSize);
    //修复长度后下个文件没对上
    if (!isRight(centralList, fixEntry, nextEntry)) {
      while (fixedSize != -1) {
        long nextSize = fixedSize + ZIP_ENTRY_FILE_HEARD.length;
        if (callback != null) {
          callback.onFix(fileChannel.size(), fixEntry.getFileStartPosition() + nextSize);
        }
        fixedSize = ByteUtil
            .getNextTokenSize(fileChannel, fixEntry.getFileStartPosition(),
                fixEntry.getFileStartPosition() + nextSize,
                ZIP_ENTRY_FILE_HEARD, ZIP_ENTRY_DIR_HEARD);
        nextEntry = getNextBdyZipEntry(fileChannel,
            fixEntry.getFileStartPosition() + fixedSize);
        if (isRight(centralList, fixEntry, nextEntry)) {
          break;
        }
      }
      return fixedSize;
    } else {
      return fixedSize;
    }
  }

  private static boolean isRight(List<String> centralList, BdyZipEntry fixEntry,
      BdyZipEntry nextEntry) {
    if (Arrays.equals(nextEntry.getHeader(), Arrays.copyOfRange(ZIP_ENTRY_DIR_HEARD, 0, 4))) {
      return true;
    } else {
      for (int i = 0; i < centralList.size(); i++) {
        if (i + 1 < centralList.size()
            && centralList.get(i).equals(fixEntry.getFileName())
            && centralList.get(i + 1).equals(nextEntry.getFileName())) {
          return true;
        }
      }
      return false;
    }
  }

  public static List<BdyZipEntry> getFixedEntryList(FileChannel fileChannel,
      BdyUnzipCallback callback) throws IOException {
    List<BdyZipEntry> list = new ArrayList<>();
    List<String> centralList = getCentralList(fileChannel);
    fileChannel.position(0);
    while (true) {
      BdyZipEntry entry = getNextFixedBdyZipEntry(fileChannel, centralList, callback);
      list.add(entry);
      if (entry.isEnd()) {
        if (callback != null) {
          callback.onFixDone(list);
        }
        return list;
      }
    }
  }

  private final static int EOCD_SIZE = 22;
  private final static int CENTRAL_COUNT_OFFSET = 10;
  private final static int CENTRAL_SIZE = 46;
  private final static int CENTRAL_NAME_OFFSET = 28;

  /**
   * 取zip中所有文件和文件夹信息 doc https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-6.2.0.txt
   */
  private static List<String> getCentralList(FileChannel fileChannel) throws IOException {
    List<String> centralList = new ArrayList<>();
    //read EOCD
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    fileChannel.position(fileChannel.size() - EOCD_SIZE + CENTRAL_COUNT_OFFSET);
    fileChannel.read(byteBuffer);
    byteBuffer.flip();
    byte[] bts2 = new byte[2];
    //offset 10
    byteBuffer.get(bts2);
    int centralCount = (int) ByteUtil.btsToNumForSmall(bts2);
    //offset 12
    byteBuffer.get(bts2);
    long centralSize = ByteUtil.btsToNumForSmall(bts2);
    fileChannel.position(fileChannel.size() - centralSize - EOCD_SIZE);
    //read Central directory list
    ByteBuffer centralBuffer = ByteBuffer.allocate(CENTRAL_SIZE);
    for (int i = 0; i < centralCount; i++) {
      centralBuffer.clear();
      fileChannel.read(centralBuffer);
      centralBuffer.flip();
      //read Central name size
      centralBuffer.position(CENTRAL_NAME_OFFSET);
      centralBuffer.get(bts2);
      int nameSize = (int) ByteUtil.btsToNumForSmall(bts2);
      ByteBuffer nameBuffer = ByteBuffer.allocate(nameSize);
      fileChannel.read(nameBuffer);
      centralList.add(new String(nameBuffer.array(), "GB18030"));
    }
    return centralList;
  }

  public static void unzip(String path, String toPath, BdyUnzipCallback callback)
      throws Exception {
    try {
      if (callback != null) {
        callback.onStart();
      }
      File zipFile = new File(path);
      if (!FileUtil.exists(toPath)) {
        FileUtil.createDirSmart(toPath);
      }
      File toDir = new File(toPath);
      try (
          FileChannel fileChannel = new RandomAccessFile(zipFile, "rw").getChannel()
      ) {
        List<BdyZipEntry> list = getFixedEntryList(fileChannel, callback);
        for (BdyZipEntry zipEntry : list) {
          if (callback != null) {
            callback.onEntryStart(zipEntry);
          }
          /*long fileSize = zipEntry.getCompressedSize();
          if (zipEntry.isDir()) {
            FileUtil.createDirSmart(toPath + File.separator + zipEntry.getFileName());
            if (callback != null) {
              callback.onEntryWrite(0, 0);
            }
          } else {
            File unzipFile = FileUtil
                .createFileSmart(toDir.getPath() + File.separator + zipEntry.getFileName());
            FileChannel unzipChannel = new RandomAccessFile(unzipFile, "rw").getChannel();
            long position = zipEntry.getFileStartPosition();
            long remaining = fileSize;
            while (remaining > 0) {
              long size = remaining > _512M ? _512M : remaining;
              long transferred = fileChannel.transferTo(position, size, unzipChannel);
              remaining -= transferred;
              position += transferred;
              if (callback != null) {
                callback.onEntryWrite(fileSize, transferred);
              }
            }
            unzipChannel.close();
          }*/
        }
        if (callback != null) {
          callback.onDone();
        }
      }
    } catch (Exception e) {
      if (callback != null) {
        callback.onError(e);
      }
      throw e;
    }
  }


  public static void unzip(String path, String toPath)
      throws Exception {
    unzip(path, toPath, null);
  }

  public static void main(String[] args) throws Exception {
//    unzip(args[0], args[1], new TestUnzipCallback());
    unzip("f:/down/packbu7t.zip", "f:/down/packbu7t", new TestUnzipCallback());
    unzip("f:/down/pack13.zip", "f:/down/pack13", new TestUnzipCallback());
    unzip("f:/down/win7double.zip", "f:/down/win7double", new TestUnzipCallback());
    unzip("f:/idm/测试解压4G以上.zip", "f:/idm/测试解压4G以上", new TestUnzipCallback());
    unzip("f:/idm/鬼子来了.zip", "f:/idm/鬼子来了", new TestUnzipCallback());
    /*FileChannel fileChannel = new RandomAccessFile("f:/idm/win7pack.zip", "rw")
        .getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate(12);
    fileChannel.position(fileChannel.size() - byteBuffer.capacity());
    fileChannel.read(byteBuffer);
    byteBuffer.flip();
    byte[] bts4 = new byte[4];
    byte[] bts2 = new byte[2];
    byteBuffer.get(bts2);
    int entryCount = (int) ByteUtil.btsToNumForSmall(bts2);
    byteBuffer.get(bts2);
    long centralSize = ByteUtil.btsToNumForSmall(bts2);
    ByteBuffer centralBuffer = ByteBuffer.allocate(46);
    fileChannel.position(fileChannel.size() - centralSize - 22);
    for (int i = 1; i <= entryCount; i++) {
      fileChannel.read(centralBuffer);
      centralBuffer.flip();
      int skipSize = printBuffer(centralBuffer,
          new int[]{4, 2, 2, 2, 2, 2, 2, 4, 4, 4, 2, 2, 2, 2, 2, 4, 4});
      ByteBuffer nameBuffer = ByteBuffer.allocate(skipSize);
      fileChannel.read(nameBuffer);
      nameBuffer.flip();
      System.out.println(Charset.forName("GB18030").decode(nameBuffer));
      centralBuffer.clear();
    }*/
   /* FileChannel fileChannel = new RandomAccessFile("f:/idm/win7pack.zip", "rw").getChannel();
    getCentralList(fileChannel);*/
    /*BdyZipEntry entry;
    while ((entry = getNextBdyZipEntry(fileChannel)) != null) {
      fileChannel.position(fileChannel.position() + entry.getCompressedSize());
      System.out.println(entry.getFileName());
      if (entry.isEnd()) {
        break;
      }
    }*/
  }

  private static int printBuffer(ByteBuffer byteBuffer, int[] array) {
    int offset = 0;
    int n = 0;
    int m = 0;
    int k = 0;
    for (int size : array) {
      byte[] bts = new byte[size];
      byteBuffer.get(bts);
      if (offset == 42) {
      }
      if (offset == 28) {
        n = (int) ByteUtil.btsToNumForSmall(bts);
      }
      if (offset == 30) {
        m = (int) ByteUtil.btsToNumForSmall(bts);
      }
      if (offset == 32) {
        k = (int) ByteUtil.btsToNumForSmall(bts);
      }
      offset += size;
    }
    return n + m + k;
  }

  /**
   * 检查是否为百度云合并下载ZIP
   */
  public static boolean isBdyZip(String filePath) throws IOException {
    try (
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw")
    ) {
      byte[] speHeard = new byte[10];
      raf.read(speHeard);
      if (Arrays.equals(speHeard, ZIP_ENTRY_FILE_HEARD)
          || Arrays.equals(speHeard, ZIP_ENTRY_DIR_HEARD)) {
        return true;
      }
    }
    return false;
  }

  public final static String ON_START = "onStart";
  public final static String ON_FIX = "onFix";
  public final static String ON_FIX_DONE = "onFixDone";
  public final static String ON_ENTRY_START = "onEntryStart";
  public final static String ON_ENTRY_WRITE = "onEntryWrite";
  public final static String ON_DONE = "onDone";
  public final static String ON_ERROR = "onError";

  public interface BdyUnzipCallback {

    void onStart();

    void onFix(long totalSize, long fixSize);

    void onFixDone(List<BdyZipEntry> list);

    void onEntryStart(BdyZipEntry entry);

    void onEntryWrite(long totalSize, long writeSize);

    void onDone();

    void onError(Exception e);
  }

  static class TestUnzipCallback implements BdyUnzipCallback {

    @Override
    public void onStart() {
      System.out.println("onStart");
    }

    @Override
    public void onFix(long totalSize, long fixSize) {
      System.out.println("onFix:" + totalSize + " " + fixSize);
    }

    @Override
    public void onFixDone(List<BdyZipEntry> list) {
      System.out.println("onFixDone");
    }

    @Override
    public void onEntryStart(BdyZipEntry entry) {
      System.out.println("onEntryStart:" + entry.getFileName());
    }

    @Override
    public void onEntryWrite(long totalSize, long writeSize) {

    }

    @Override
    public void onDone() {
      System.out.println("onDone");
    }

    @Override
    public void onError(Exception e) {

    }
  }
}

