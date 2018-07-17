package org.pdown.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

  public static void main(String[] args) throws IOException {
    Process process = Runtime.getRuntime().exec("./main/proxyee-down-main");
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
    }
  }
}
