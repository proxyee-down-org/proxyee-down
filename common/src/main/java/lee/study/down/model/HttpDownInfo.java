package lee.study.down.model;

import io.netty.handler.codec.http.HttpRequest;
import java.io.Serializable;
import lee.study.proxyee.proxy.ProxyConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpDownInfo implements Serializable {

  private static final long serialVersionUID = 9151154678187498760L;
  private TaskInfo taskInfo;
  private HttpRequest request;
  private ProxyConfig proxyConfig;
}
