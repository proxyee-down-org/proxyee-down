package org.pdown.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

public class Runner {

  private static final String JAVA_CMD_PATH = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
  private static final String MAIN_JAR_PATH = "main/proxyee-down-main.jar";
  private static final String MAIN_JAR_BAK_PATH = MAIN_JAR_PATH + ".bak";

  public static void main(String[] args) throws IOException {
    fork();
  }

  private static void fork() {
    File bakFile = new File(MAIN_JAR_BAK_PATH);
    if (bakFile.exists()) {
      //更新后删除旧版本
      for (int i = 0; i < 30; i++) {
        if (new File(MAIN_JAR_PATH).delete()) {
          bakFile.renameTo(new File(MAIN_JAR_PATH));
          break;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
          }
        }
      }
    }
    try {
      Process process = Runtime.getRuntime().exec(new String[]{
          JAVA_CMD_PATH,
          "-jar",
          //"-Dfile.encoding=GBK",
          //"-Dapple.awt.UIElement=true",
          "-Xms128m",
          "-Xmx384m",
          MAIN_JAR_PATH
      });
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      boolean isClose = false;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
        if ("proxyee-down-exit".equals(line)) {
          isClose = true;
          break;
        }
      }
      if (isClose) {
        process.destroy();
        fork();
      }
    } catch (Throwable throwable) {
      alert(throwable.getMessage());
      System.exit(1);
    }
  }

  private static void alert(String msg) {
    JOptionPane.showMessageDialog(null, msg, "title", JOptionPane.ERROR_MESSAGE);
  }
}
