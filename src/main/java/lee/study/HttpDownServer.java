package lee.study;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.LinkedHashMap;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer implements InitializingBean {

  public static final NioEventLoopGroup LOOP_GROUP = new NioEventLoopGroup(1);

  public static final Map<String, HttpDownInfo> DOWN_CONTENT = new ConcurrentHashMap<>();
  public static final Map<String, WebSocketSession> WS_CONTENT = new ConcurrentHashMap<>();

  public static int VIEW_SERVER_PORT;

  @Value("${view.server.port}")
  private int viewServerPort;

  @Override
  public void afterPropertiesSet() throws Exception {
    VIEW_SERVER_PORT = viewServerPort;
  }

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
