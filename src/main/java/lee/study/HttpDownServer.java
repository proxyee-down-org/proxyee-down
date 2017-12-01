package lee.study;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.intercept.BdyBatchDownIntercept;
import lee.study.intercept.BdyIntercept;
import lee.study.intercept.HttpDownIntercept;
import lee.study.intercept.HttpDownSniffIntercept;
import lee.study.model.HttpDownInfo;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer {

  public static NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);

  public static final Map<String, HttpDownInfo> downContent = new ConcurrentHashMap<>();
  public static Map<String, WebSocketSession> wsContent = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {
    SpringApplication.run(HttpDownServer.class, args);
    //监听http下载请求
    new HttpProxyServer().proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new BdyIntercept());
        pipeline.addLast(new HttpDownSniffIntercept());
        pipeline.addLast(new BdyBatchDownIntercept());
        pipeline.addLast(new HttpDownIntercept());
      }
    }).start(9999);
  }
}
