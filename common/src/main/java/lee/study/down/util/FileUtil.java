package lee.study.down.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Stack;
import java.util.UUID;

public class FileUtil {

  /**
   * 文件或目录是否存在
   */
  public static boolean exists(String path) {
    return new File(path).exists();
  }

  /**
   * 文件或目录是否存在
   */
  public static boolean existsAny(String... paths) {
    return Arrays.stream(paths).anyMatch(path -> new File(path).exists());
  }

  /**
   * 删除文件或文件夹
   */
  public static void deleteIfExists(File file) throws IOException {
    if (file.exists()) {
      if (file.isFile()) {
        if (!file.delete()) {
          throw new IOException("Delete file failure,path:" + file.getAbsolutePath());
        }
      } else {
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
          for (File temp : files) {
            deleteIfExists(temp);
          }
        }
        if (!file.delete()) {
          throw new IOException("Delete file failure,path:" + file.getAbsolutePath());
        }
      }
    }
  }

  /**
   * 删除文件或文件夹
   */
  public static void deleteIfExists(String path) throws IOException {
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
    File file = createFileSmart(path);
    if (OsUtil.isWindows()) {
      Files.setAttribute(file.toPath(), "dos:hidden", isHidden);
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
    if (OsUtil.isWindows()) {
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

  public static File createFileSmart(String path) throws IOException {
    try {
      File file = new File(path);
      if (file.exists()) {
        file.delete();
        file.createNewFile();
      } else {
        createDirSmart(file.getParent());
        file.createNewFile();
      }
      return file;
    } catch (IOException e) {
      throw new IOException("createFileSmart=" + path, e);
    }
  }

  public static File createDirSmart(String path) throws IOException {
    try {
      File file = new File(path);
      if (file.exists()) {
        file.delete();
        file.mkdir();
      } else {
        Stack<File> stack = new Stack<>();
        File temp = new File(path);
        while (temp != null) {
          stack.push(temp);
          temp = temp.getParentFile();
        }
        while (stack.size() > 0) {
          File dir = stack.pop();
          if (!dir.exists()) {
            dir.mkdir();
          }
        }
      }
      return file;
    } catch (Exception e) {
      throw new IOException("createDirSmart=" + path, e);
    }
  }

  /**
   * 获取目录所属磁盘剩余容量
   */
  public static long getDiskFreeSize(String path) {
    File file = new File(path);
    while (true) {
      if (file.getParent() == null) {
        return file.getFreeSpace();
      } else {
        file = file.getParentFile();
      }
    }
  }

  public static void unmap(MappedByteBuffer mappedBuffer) throws IOException {
    try {
      Class<?> clazz = Class.forName("sun.nio.ch.FileChannelImpl");
      Method m = clazz.getDeclaredMethod("unmap", MappedByteBuffer.class);
      m.setAccessible(true);
      m.invoke(clazz, mappedBuffer);
    } catch (Exception e) {
      throw new IOException("LargeMappedByteBuffer close", e);
    }
  }

  /**
   * 去掉后缀名
   */
  public static String getFileNameNoSuffix(String fileName) {
    int index = fileName.lastIndexOf(".");
    if (index != -1) {
      return fileName.substring(0, index);
    }
    return fileName;
  }

  public static void initFile(String path, boolean isHidden) throws IOException {
    initFile(path, null, isHidden);
  }

  public static void initFile(String path, InputStream input, boolean isHidden) throws IOException {
    if (exists(path)) {
      try (
          RandomAccessFile raf = new RandomAccessFile(path, "rw")
      ) {
        raf.setLength(0);
      }
    } else {
      FileUtil.createFile(path, isHidden);
    }
    if (input != null) {
      try (
          RandomAccessFile raf = new RandomAccessFile(path, "rw")
      ) {
        byte[] bts = new byte[8192];
        int len;
        while ((len = input.read(bts)) != -1) {
          raf.write(bts, 0, len);
        }
      } finally {
        input.close();
      }
    }
  }

  public static boolean canWrite(String path) {
    File file = new File(path);
    File test = new File(
        file.getParent() + File.separator + UUID.randomUUID().toString() + ".test");
    try {
      test.createNewFile();
      test.delete();
    } catch (IOException e) {
      return false;
    }
    return true;
  }
}
