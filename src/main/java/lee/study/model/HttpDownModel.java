package lee.study.model;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import lee.study.down.HttpDown;

public class HttpDownModel {

  private HttpDown.DownInfo downInfo;
  private HttpRequest request;
  private HttpHeaders headers;

  public HttpDownModel() {
  }

  public HttpDownModel(HttpDown.DownInfo downInfo, HttpRequest request, HttpHeaders headers) {
    this.downInfo = downInfo;
    this.request = request;
    this.headers = headers;
  }

  public HttpDown.DownInfo getDownInfo() {
    return downInfo;
  }

  public void setDownInfo(HttpDown.DownInfo downInfo) {
    this.downInfo = downInfo;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  public void setHeaders(HttpHeaders headers) {
    this.headers = headers;
  }
}
