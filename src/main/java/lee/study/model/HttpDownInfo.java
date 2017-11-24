package lee.study.model;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpDownInfo {
  private final static AtomicInteger seq = new AtomicInteger();

  private int id;
  private TaskInfo taskInfo;
  private HttpRequest request;
  private HttpHeaders headers;

  public HttpDownInfo(TaskInfo taskInfo, HttpRequest request,
      HttpHeaders headers) {
    this.id = seq.getAndIncrement();
    this.taskInfo = taskInfo;
    this.request = request;
    this.headers = headers;

    this.taskInfo.setId(this.id);
  }
}
