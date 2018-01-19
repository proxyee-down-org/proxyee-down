package lee.study.down.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Stack;

public class FileUtil {

  public static RandomAccessFile getRafFile(String path) throws IOException {
    return new RandomAccessFile(path, "rw");
  }

  /**
   * 文件或目录是否存在
   */
  public static boolean exists(String path) {
    return new File(path).exists();
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
    File file = new File(path);
    deleteIfExists(file);
    file.createNewFile();
    File newFile = new File(path);
    newFile.createNewFile();
    if (OsUtil.isWindows()) {
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
    File file = new File(path);
    if (file.exists()) {
      file.delete();
      file.createNewFile();
    } else {
      createDirSmart(file.getParent());
      file.createNewFile();
    }
    return file;
  }

  public static File createDirSmart(String path) throws IOException {
    File file = new File(path);
    if (file.exists()) {
      file.delete();
      file.mkdir();
    } else {
      Stack<File> stack = new Stack<>();
      while (file != null) {
        stack.push(file);
        file = file.getParentFile();
      }
      while (stack.size() > 0) {
        File dir = stack.pop();
        if (!dir.exists()) {
          dir.mkdir();
        }
      }
    }
    return file;
  }

  public static void main(String[] args) throws Exception {
    String path = "F:\\百度云合并下载研究\\test.txt";
    RandomAccessFile raf2 = new RandomAccessFile(path, "rw");
    raf2.setLength(1024);
    raf2.close();
    RandomAccessFile raf3 = new RandomAccessFile(path, "rw");
    raf3.setLength(0);
    raf3.close();
    /*FileChannel fileChannel = new RandomAccessFile(path, "rw").getChannel();
    MappedByteBuffer byteBuffer1 = fileChannel.map(MapMode.READ_WRITE,0,1000);
    byteBuffer1.put(new byte[]{1,2,3,4,5});
    byte[] bytes = new byte[5];
    byteBuffer1.flip();
    byteBuffer1.get(bytes);
    System.out.println(Arrays.toString(bytes));*/
  }
}
