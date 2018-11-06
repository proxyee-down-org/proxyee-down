package org.pdown.gui.extension.mitm.intercept;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.util.ByteUtil;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AsciiString;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import org.pdown.gui.DownApplication;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ContentScript;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 通过代理服务器代理ajax请求，避免浏览器CORS问题
 */
public class AjaxIntercept extends HttpProxyIntercept {

  private static final String PROXY_SEND_KEY = "X-Proxy-Send";

  private boolean proxyFlag;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
    proxyFlag = httpRequest.headers().contains(PROXY_SEND_KEY);
    super.beforeRequest(clientChannel, httpRequest, pipeline);
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
    if (proxyFlag) {
      httpResponse.setStatus(HttpResponseStatus.OK);
      proxyChannel.close();
      ObjectMapper objectMapper = new ObjectMapper();
      LastHttpContent content = new DefaultLastHttpContent();
      String proxyRequestRaw = URLDecoder.decode(pipeline.getHttpRequest().headers().get(PROXY_SEND_KEY), "utf-8");
      try {
        ProxyRequest proxyRequest = objectMapper.readValue(proxyRequestRaw, ProxyRequest.class);
        ProxyResponse proxyResponse = doRequest(proxyRequest);
        httpResponse.setStatus(HttpResponseStatus.valueOf(proxyResponse.getStatus()));
        content.content().writeBytes(proxyResponse.getData());
      } catch (IOException e) {
        e.printStackTrace();
        httpResponse.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
      }
      httpResponse.headers().remove(HttpHeaderNames.CONTENT_ENCODING);
      httpResponse.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
      httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.content().readableBytes());
      httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, AsciiString.cached("application/json; charset=utf-8"));
      super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
      clientChannel.writeAndFlush(content);
    } else {
      super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }
  }

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!proxyFlag) {
      super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }
  }

  private ProxyResponse doRequest(ProxyRequest proxyRequest) throws IOException {
    URL url = new URL(proxyRequest.getUrl());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(proxyRequest.getMethod().toUpperCase());
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    connection.setDoInput(true);
    if (proxyRequest.getData() != null) {
      connection.setDoOutput(true);
      try (
          OutputStream output = connection.getOutputStream()
      ) {
        ObjectMapper objectMapper = new ObjectMapper();
        output.write(objectMapper.writeValueAsBytes(proxyRequest.getData()));
        output.flush();
      }
    }
    ProxyResponse proxyResponse = new ProxyResponse();
    proxyResponse.setStatus(connection.getResponseCode());
    try (
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream input = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()
    ) {
      byte[] bts = new byte[8192];
      int len;
      while ((len = input.read(bts)) != -1) {
        output.write(bts, 0, len);
      }
      proxyResponse.setData(output.toByteArray());
      return proxyResponse;
    }
  }

  static class ProxyRequest {

    private String method;
    private String url;
    private Map<String, Object> data;

    public String getMethod() {
      return method;
    }

    public void setMethod(String method) {
      this.method = method;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public Map<String, Object> getData() {
      return data;
    }

    public void setData(Map<String, Object> data) {
      this.data = data;
    }
  }

  static class ProxyResponse {

    private int status;
    private byte[] data;

    public int getStatus() {
      return status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public byte[] getData() {
      return data;
    }

    public void setData(byte[] data) {
      this.data = data;
    }
  }
}
