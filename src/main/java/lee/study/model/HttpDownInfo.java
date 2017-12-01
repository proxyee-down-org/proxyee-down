package lee.study.model;

import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lee.study.down.HttpDown;
import lee.study.model.HttpRequestInfo.HttpVer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpDownInfo implements Serializable {

  private TaskInfo taskInfo;
  private HttpRequest request;

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.set("1111", "bbb");
    httpHeaders.set("2222", new String[]{"cccc", "dddd"});
    System.out.println(httpHeaders.getAsString("2222"));
    HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/",
        httpHeaders);
    Map<String,String> heads = new HashMap<>();
    heads.put("1111", "bbb");
    heads.put("2222", "ccc");
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1,HttpMethod.GET.toString(),"/",heads);
    HttpDownInfo httpDownInfo = new HttpDownInfo( new TaskInfo("1","测试", 111,
        true, 5, "", 0, 0, 0, null),requestInfo);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream(baos);
    outputStream.writeObject(httpDownInfo);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    HttpDownInfo hdi = (HttpDownInfo) ois.readObject();
    System.out.println(hdi.toString());
  }
}
