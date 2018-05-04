package lee.study.down;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import lee.study.down.util.OsUtil;
import lee.study.down.util.PathUtil;

public class Bootstrap {

  public static void main(String[] args) throws Exception {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (process != null) {
        process.destroy();
      }
    }));
    fork();
  }

  private static final String MAIN_PATH = PathUtil.ROOT_PATH + "main" + File.separator;
  private static final String CORE_PATH = MAIN_PATH + "proxyee-down-core.jar";
  private static final String UPDATE_PATH = MAIN_PATH + "proxyee-down-core.jar.bak";
  private static final String SHELL_PARAMS = " -Dfile.encoding=GBK -Xms128m -Xmx128m -jar ";
  private static Process process;
  private static String SHELL;

  static {
    String execPath = File.separator + "bin" + File.separator + "java";
    String javaHome = System.getProperty("java.home") + execPath;
    if (OsUtil.isWindows()) {
      SHELL = "\"" + javaHome + "\"" + SHELL_PARAMS + "\"" + CORE_PATH + "\"";
    } else {
      SHELL = javaHome + SHELL_PARAMS + CORE_PATH;
    }
  }

  private static void fork() throws Exception {
    process = Runtime.getRuntime().exec(SHELL);
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    boolean isUpdate = false;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
      if ("proxyee-down-update".equals(line)) {
        isUpdate = true;
        break;
      }
    }
    if (isUpdate) {
      process.destroy();
      replaceJar();
      fork();
    }
  }

  private static void replaceJar() throws InterruptedException {
    File updateFile = new File(UPDATE_PATH);
    File beforeFile = new File(CORE_PATH);
    if (updateFile.exists()) {
      if (beforeFile.exists()) {
        if (!delete(beforeFile)) {
          System.exit(1);
        }
      }
      updateFile.renameTo(new File(CORE_PATH));
    }
  }

  private static boolean delete(File file) throws InterruptedException {
    for (int i = 0; i < 30; i++) {
      if (file.delete()) {
        return true;
      }
      Thread.sleep(1000);
    }
    return false;
  }
}
