package lee.study;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.intercept.HttpDownIntercept;
import lee.study.model.HttpDownInfo;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer {

  public static NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);

  public static final Map<Integer,HttpDownInfo> downContent = new ConcurrentHashMap<>();
  public static Map<String,WebSocketSession> wsContent = new ConcurrentHashMap<>();


  private void start(int port) {
    new HttpProxyServer().proxyInterceptFactory(() -> new HttpDownIntercept()).start(port);
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(HttpDownServer.class, args);
    new HttpDownServer().start(9999);
  }
}
