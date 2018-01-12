package lee.study.down;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

  public static void main(String[] args) throws IOException {
    Process process = Runtime.getRuntime().exec("java -jar ./target/proxyee-down.jar");
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = br.readLine()) != null) {
      if ("proxyee-down-update".equals(line)) {
        process.destroy();
      }
    }
  }
}
