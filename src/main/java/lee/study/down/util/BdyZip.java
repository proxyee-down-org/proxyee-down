package lee.study.down.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BdyZip {

  private static final Logger LOGGER = LoggerFactory.getLogger(BdyZip.class);

  private static final byte[] ZIP_ENTRY_FILE_HEARD = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x0A, 0x00,
      0x00, 0x00, 0x00, 0x00};
  private static final byte[] ZIP_ENTRY_DIR_HEARD = new byte[]{0x50, 0x4B, 0x01, 0x02, 0x00, 0x00,
      0x0A, 0x00, 0x00, 0x00};

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
    return zipEntry;
  }

  public static void unzip(String path, String toPath) throws IOException {
    File zipFile = new File(path);
    File toDir = FileUtil.createDirSmart(toPath);
    FileChannel fileChannel = new RandomAccessFile(zipFile, "rw").getChannel();
    boolean isEnd = false;
    while (!isEnd) {
      BdyZipEntry bdyZipEntry = getNextBdyZipEntry(fileChannel);
      long fileSize = bdyZipEntry.getCompressedSize();
      if (ByteUtil.matchToken(fileChannel, fileChannel.position() + fileSize, ZIP_ENTRY_DIR_HEARD)) {
        isEnd = true;
      } else if (!ByteUtil.matchToken(fileChannel, fileChannel.position() + fileSize,
          ZIP_ENTRY_FILE_HEARD)) {
        //找到真实文件长度
        fileSize = ByteUtil.getNextTokenSize(fileChannel, ZIP_ENTRY_FILE_HEARD, ZIP_ENTRY_DIR_HEARD);
        if (ByteUtil.matchToken(fileChannel, fileChannel.position() + fileSize, ZIP_ENTRY_DIR_HEARD)) {
          isEnd = true;
        }
      }
      LOGGER.debug("bdyUnzip:"+bdyZipEntry.getFileName()+"\t"+fileSize+"\t"+isEnd);
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
}
