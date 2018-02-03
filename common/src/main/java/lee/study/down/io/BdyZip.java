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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public class BdyZip {

  private static final byte[] ZIP_ENTRY_FILE_HEARD = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x0A,
      0x00, 0x00, 0x00, 0x00, 0x00};
  private static final byte[] ZIP_ENTRY_DIR_HEARD = new byte[]{0x50, 0x4B, 0x01, 0x02, 0x00,
      0x00, 0x0A, 0x00, 0x00, 0x00};
  private static final long _4G = 1024 * 1024 * 1024 * 4L - 1;
  private static final long _512M = 1024 * 1024 * 521L;

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
    private boolean isDir;
    private boolean isFix;
    private long fileStartPosition;
    private boolean isEnd;

    public long getEntrySize() {
      return 30 + compressedSize;
    }
  }

  public static List<BdyZipEntry> getFixedEntryList(FileChannel fileChannel,
      BdyUnzipCallback callback) throws IOException {
    List<BdyZipEntry> list = new ArrayList<>();
    while (true) {
      BdyZipEntry entry = getNextBdyZipEntry(fileChannel, list, null, 0, callback);
      if (entry.isEnd()) {
        return list;
      }
    }
  }

  public static BdyZipEntry getNextBdyZipEntry(FileChannel fileChannel, List<BdyZipEntry> entryList,
      BdyZipEntry fixEntry, long skipSize, BdyUnzipCallback callback)
      throws IOException {
    BdyZipEntry zipEntry;
    if (fixEntry == null) {
      zipEntry = new BdyZipEntry();
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
        ByteBuffer extraFieldBuffer = ByteBuffer.allocate((int) zipEntry.getExtraFieldLength());
        fileChannel.read(extraFieldBuffer);
        zipEntry.setExtraField(extraFieldBuffer.array());
      }
      if (zipEntry.getCompressedSize() == 0
          && (zipEntry.getFileName().length() == 0
          || "/".equals(zipEntry.getFileName().substring(zipEntry.getFileName().length() - 1)))) {
        zipEntry.setDir(true);
      }
      zipEntry.setFileStartPosition(fileChannel.position());
      entryList.add(zipEntry);
    } else {
      zipEntry = fixEntry;
    }
    if (callback != null) {
      callback.onFix(fileChannel.size(), fileChannel.position());
    }
    if (ByteUtil
        .matchToken(fileChannel,
            zipEntry.getFileStartPosition(),
            zipEntry.getFileStartPosition() + zipEntry.getCompressedSize() + skipSize,
            ZIP_ENTRY_DIR_HEARD)) {
      zipEntry.setEnd(true);
    } else if (!ByteUtil
        .matchToken(fileChannel,
            zipEntry.getFileStartPosition(),
            zipEntry.getFileStartPosition() + zipEntry.getCompressedSize() + skipSize,
            ZIP_ENTRY_FILE_HEARD)) {
      long totalSkipSize = skipSize > 0 ? zipEntry.getCompressedSize() + skipSize : _4G;
      long fixSize = ByteUtil
          .getNextTokenSize(fileChannel, zipEntry.getFileStartPosition(),
              zipEntry.getFileStartPosition() + totalSkipSize,
              ZIP_ENTRY_FILE_HEARD, ZIP_ENTRY_DIR_HEARD);
      if (fixSize != -1) {
        zipEntry.setFix(true);
        fileChannel.position(fileChannel.position() + fixSize);
        zipEntry.setUnCompressedSize(fixSize);
        zipEntry.setCompressedSize(fixSize);
        if (ByteUtil
            .matchToken(fileChannel, zipEntry.getFileStartPosition() + zipEntry.getCompressedSize(),
                ZIP_ENTRY_DIR_HEARD)) {
          zipEntry.setEnd(true);
        }
      } else {
        //找到最近一个修复错误的文件重新修复
        long needSkipSize = 0;
        int index = -1;
        for (int i = entryList.size() - 1; i >= 0; i--) {
          BdyZipEntry temp = entryList.get(i);
          if (temp.isFix()) {
            index = i;
            break;
          } else {
            needSkipSize += temp.getEntrySize();
            entryList.remove(i);
          }
        }
        BdyZipEntry needFixEntry = entryList.get(index);
        getNextBdyZipEntry(fileChannel, entryList, needFixEntry, needSkipSize, callback);
        if (fileChannel.position() >= fileChannel.size()) {
          zipEntry.setEnd(true);
        }
      }
    } else {
      fileChannel.position(fileChannel.position() + zipEntry.getCompressedSize());
    }
    if (fileChannel.position() >= fileChannel.size()) {
      zipEntry.setEnd(true);
    }
    return zipEntry;
  }

  public static void unzip(String path, String toPath, BdyUnzipCallback callback)
      throws IOException {
    try {
      if (callback != null) {
        callback.onStart();
      }
      File zipFile = new File(path);
      File toDir = FileUtil.createDirSmart(toPath);
      try (
          FileChannel fileChannel = new RandomAccessFile(zipFile, "rw").getChannel()
      ) {
        List<BdyZipEntry> list = getFixedEntryList(fileChannel, callback);
        if (callback != null) {
          callback.onFixDone(list);
        }
        for (BdyZipEntry zipEntry : list) {
          if (callback != null) {
            callback.onEntryStart(zipEntry);
          }
          long fileSize = zipEntry.getCompressedSize();
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
          }
        }
        if (callback != null) {
          callback.onDone();
        }
      }
    } catch (IOException e) {
      if (callback != null) {
        callback.onError(e);
      }
      throw e;
    }
  }


  public static void unzip(String path, String toPath)
      throws IOException {
    unzip(path, toPath, null);
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


  public static void main(String[] args) throws IOException {
//    unzipTest("f:/down/【批量下载】新建文本文档等.zip", "f:/down");
//    unzipTest("f:/down/【批量下载】英语国家社会与文化-uni等.zip", "f:/down");
//    unzipTest("f:/down/【批量下载】test2ddd测试等.zip", "f:/down");
    FileChannel fileChannel = new RandomAccessFile("f:/down/【批量下载】英语国家社会与文化-uni等.zip", "rw")
        .getChannel();
    List<BdyZipEntry> list = getFixedEntryList(fileChannel, null);
    //4,658,989,306
    //4,658,989,306
//    unzip("f:/down/【批量下载】新建文本文档等.zip", "f:/down");

  }

}
