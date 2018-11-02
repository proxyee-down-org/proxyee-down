package org.pdown.gui.extension.jsruntime.polyfill.property;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.springframework.util.StringUtils;

public class XMLHttpRequest {

  private String method;
  private String url;

  public Function onreadystatechange;
  public int readyState = 0;
  public int status = 0;
  public String responseText;

  private Map<String, String> customRequestHeads = new LinkedHashMap<>();
  private Map<String, String> responseHeads = new LinkedHashMap<>();

  public void setRequestHeader(String header, String value) {
    customRequestHeads.put(header, value);
  }

  public String getResponseHeader(String header) {
    return responseHeads.get(header.toLowerCase());
  }

  public void open(String method, String url) {
    this.method = method;
    this.url = url;
  }

  public void open(String method, String url, boolean async) {
    this.open(method, url);
  }

  private static String DEFAULT_UA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";

  public void send(String data) throws IOException {
    URL u = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
    readystatechange(1);
    connection.setRequestMethod(method.toUpperCase());
    connection.setRequestProperty("User-Agent", DEFAULT_UA);
    customRequestHeads.entrySet().stream().forEach(entry -> connection.setRequestProperty(entry.getKey(), entry.getValue()));
    connection.setDoOutput(true);
    if (data != null && data.trim().length() > 0) {
      try (
          OutputStream outputStream = connection.getOutputStream()
      ) {
        outputStream.write(data.getBytes(Charset.forName("UTF-8")));
      }
    }
    int code = connection.getResponseCode();
    connection.getHeaderFields().entrySet().forEach(entry -> {
          if (entry.getKey() != null) {
            responseHeads.put(entry.getKey().toLowerCase(), entry.getValue().stream().collect(Collectors.joining("; ")));
          }
        }
    );
    readystatechange(2, code);
    String charset = "UTF-8";
    String contentType = connection.getContentType();
    if (!StringUtils.isEmpty(contentType)) {
      Pattern pattern = Pattern.compile("charset=(.*)$", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(contentType);
      if (matcher.find()) {
        charset = matcher.group(1);
      }
    }
    InputStream inputStream = code != 200 ? connection.getErrorStream() : connection.getInputStream();
    if (responseHeads.entrySet().stream().anyMatch(entry -> "Content-Encoding".equalsIgnoreCase(entry.getKey()) && entry.getValue().matches("^.*(?i)(gzip).*$"))) {
      inputStream = new GZIPInputStream(inputStream);
    }
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))
    ) {
      readystatechange(3, code);
      responseText = reader.lines().collect(Collectors.joining("\n"));
      readystatechange(4, code);
    }
  }

  public static void main(String[] args) throws IOException {
    URL u = new URL("http://www.baidu.com");
    Proxy proxy = new Proxy(Type.SOCKS, new InetSocketAddress("127.0.0.1", 1088));
    HttpURLConnection connection = (HttpURLConnection) u.openConnection(proxy);
    System.out.println(connection.getResponseCode());
  }

  public void send() throws IOException {
    send(null);
  }

  private void readystatechange(int readyState, int status) {
    this.readyState = readyState;
    this.status = status;
    if (onreadystatechange != null) {
      onreadystatechange.apply(null);
    }
  }

  private void readystatechange(int readyState) {
    readystatechange(readyState, status);
  }
}
