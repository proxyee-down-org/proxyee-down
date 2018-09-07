package org.pdown.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;

public class Runner {

  private static final String JAVA_CMD_PATH = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
  private static final String MAIN_JAR_PATH = "main/proxyee-down-main.jar";
  private static final String MAIN_JAR_BAK_PATH = MAIN_JAR_PATH + ".bak";
  private static final String VM_OPTIONS_PATH = "main/run.cfg";

  private static List<String> VM_OPTIONS;

  public static void main(String[] args) throws IOException {
    VM_OPTIONS = parseVmOptions();
    fork();
  }

  private static List<String> parseVmOptions() {
    File file = Paths.get(VM_OPTIONS_PATH).toFile();
    if (!file.exists()) {
      try {
        file.createNewFile();
        try (
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
        ) {
          writer.write("-Xms128m");
          writer.newLine();
          writer.write("-Xmx384m");
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      return Files.readAllLines(Paths.get(VM_OPTIONS_PATH));
    } catch (IOException e) {
    }
    return null;
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
      List<String> execParams = new ArrayList<>();
      execParams.add(JAVA_CMD_PATH);
      execParams.add("-jar");
      if (VM_OPTIONS == null) {
        execParams.add("-Xms128m");
        execParams.add("-Xmx384m");
      } else {
        for (String option : VM_OPTIONS) {
          execParams.add(option);
        }
      }
      execParams.add(MAIN_JAR_PATH);
      String[] execArray = new String[execParams.size()];
      execParams.toArray(execArray);
      Process process = Runtime.getRuntime().exec(execArray);
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
