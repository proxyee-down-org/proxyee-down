package org.pdown.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

public class Runner {

  private static final String JAVA_CMD_PATH = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
  private static final String MAIN_JAR_PATH = "../main/proxyee-down-main.jar";

  public static void main(String[] args) throws IOException {
    try {
      Process process = Runtime.getRuntime().exec(new String[]{
          JAVA_CMD_PATH,
          "-jar",
          "-Dfile.encoding=GBK",
          "-Dapple.awt.UIElement=true",
          "-Xms128m",
          "-Xmx256m",
          MAIN_JAR_PATH
      });
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
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
