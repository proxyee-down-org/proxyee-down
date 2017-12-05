package lee.study.model;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.io.Serializable;
import java.util.Map.Entry;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpRequestInfo implements HttpRequest, Serializable {

  private static final long serialVersionUID = -4521453515739581677L;
  private HttpVer version;
  private String method;
  private String uri;
  private HttpHeaders headers;



  @Override
  public HttpMethod method() {
    return new HttpMethod(method);
  }

  @Override
  @Deprecated
  public HttpMethod getMethod() {
    return method();
  }

  @Override
  public HttpRequest setMethod(HttpMethod method) {
    this.method = method.toString();
    return this;
  }

  @Override
  public String uri() {
    return uri;
  }

  @Override
  @Deprecated
  public String getUri() {
    return uri();
  }

  @Override
  public HttpRequest setUri(String uri) {
    this.uri = uri;
    return this;
  }


  @Override
  public HttpVersion protocolVersion() {
    if (version == HttpVer.HTTP_1_0) {
      return HttpVersion.HTTP_1_0;
    } else {
      return HttpVersion.HTTP_1_1;
    }
  }

  @Deprecated
  @Override
  public HttpVersion getProtocolVersion() {
    return protocolVersion();
  }

  @Override
  public HttpRequest setProtocolVersion(HttpVersion version) {
    if (version.minorVersion() == 0) {
      this.version = HttpVer.HTTP_1_0;
    } else {
      this.version = HttpVer.HTTP_1_1;
    }
    return this;
  }

  @Override
  public HttpHeaders headers() {
    return headers;
  }

  @Deprecated
  @Override
  public DecoderResult getDecoderResult() {
    return null;
  }

  @Override
  public DecoderResult decoderResult() {
    return null;
  }

  @Override
  public void setDecoderResult(DecoderResult result) {

  }

  public enum HttpVer {
    HTTP_1_0, HTTP_1_1
  }

  public static HttpRequest adapter(HttpRequest httpRequest) {
    if (httpRequest instanceof DefaultHttpRequest) {
      HttpVer version;
      if (httpRequest.protocolVersion().minorVersion() == 0) {
        version = HttpVer.HTTP_1_0;
      } else {
        version = HttpVer.HTTP_1_1;
      }
      HttpHeadsInfo httpHeadsInfo = new HttpHeadsInfo();
      for (Entry<String, String> entry : httpRequest.headers()) {
        httpHeadsInfo.set(entry.getKey(), entry.getValue());
      }
      return new HttpRequestInfo(version, httpRequest.method().toString(), httpRequest.uri(), httpHeadsInfo);
    }
    return httpRequest;
  }

  public static void main(String[] args) {
    System.out.println(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
        "/").protocolVersion().text());
  }
}
