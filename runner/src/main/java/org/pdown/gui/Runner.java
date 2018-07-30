package org.pdown.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

public class Runner {

  private static final String SHELL_PARAMS = " -Dfile.encoding=GBK -Dapple.awt.UIElement=true -Xms128m -Xmx256m -jar ";
  //  private static final String MAIN_PATH = System.getProperty("user.dir") + File.separator + "main/target" + File.separator;
  private static final String CORE_PATH = "../main/proxyee-down-main.jar";

  private static String SHELL;

  static {
    String execPath = File.separator + "bin" + File.separator + "java";
    String javaHome = System.getProperty("java.home") + execPath;
    SHELL = "\"" + javaHome + "\"" + SHELL_PARAMS + "\"" + CORE_PATH + "\"";
  }

  public static void main(String[] args) throws IOException {
    try {
      Process process = Runtime.getRuntime().exec(SHELL);
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
    }catch (Throwable throwable){
      alert(throwable.getMessage());
    }

  }

  private static void alert(String msg) {
    JOptionPane.showMessageDialog(null, msg, "title", JOptionPane.WARNING_MESSAGE);
  }
}
