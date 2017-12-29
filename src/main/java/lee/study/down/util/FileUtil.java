package lee.study.down.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FileUtil {

  public static RandomAccessFile getRafFile(String path) throws IOException {
    return new RandomAccessFile(path, "rw");
  }

  /**
   * 删除文件或文件夹
   */
  public static void deleteIfExists(File file) {
    if (file.exists()) {
      if (file.isFile()) {
        file.delete();
      } else {
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
          for (File temp : files) {
            deleteIfExists(temp);
          }
        }
        file.delete();
      }
    }
  }

  /**
   * 删除文件或文件夹
   */
  public static void deleteIfExists(String path) {
    deleteIfExists(new File(path));
  }

  /**
   * 创建文件，如果目标存在则删除
   */
  public static File createFile(String path) throws IOException {
    return createFile(path, false);
  }

  /**
   * 创建文件夹，如果目标存在则删除
   */
  public static File createDir(String path) throws IOException {
    return createDir(path, false);
  }

  /**
   * 创建文件，如果目标存在则删除
   */
  public static File createFile(String path, boolean isHidden) throws IOException {
    File file = new File(path);
    deleteIfExists(file);
    file.createNewFile();
    File newFile = new File(path);
    newFile.createNewFile();
    if(OsUtil.isWindows()){
      Files.setAttribute(newFile.toPath(), "dos:hidden", isHidden);
    }
    return file;
  }

  /**
   * 创建文件夹，如果目标存在则删除
   */
  public static File createDir(String path, boolean isHidden) throws IOException {
    File file = new File(path);
    deleteIfExists(file);
    File newFile = new File(path);
    newFile.mkdir();
    if(OsUtil.isWindows()){
      Files.setAttribute(newFile.toPath(), "dos:hidden", isHidden);
    }
    return file;
  }

  /**
   * 查看文件或者文件夹大小
   */
  public static long getFileSize(String path) {
    File file = new File(path);
    if (file.exists()) {
      if (file.isFile()) {
        return file.length();
      } else {
        long size = 0;
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
          for (File temp : files) {
            if (temp.isFile()) {
              size += temp.length();
            }
          }
        }
        return size;
      }
    }
    return 0;
  }

  public static void main(String[] args) throws Exception {
    RandomAccessFile raf1 = new RandomAccessFile("G:\\测试/test1.txt","rw");
    RandomAccessFile raf2 = new RandomAccessFile("G:\\测试/test2.txt","rw");
    raf1.setLength(97);
    raf1.write(new byte[]{1,1,1,1,1,1});
    raf2.setLength(1024*1024*1024*4L);
    raf2.write(new byte[]{2,2,2,2,2,2});
  }
}
