package lee.study.down;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.down.dispatch.HttpDownErrorCheckTask;
import lee.study.down.dispatch.HttpDownProgressPushTask;
import lee.study.down.intercept.BdyBatchDownIntercept;
import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer implements InitializingBean {

  public static final NioEventLoopGroup LOOP_GROUP = new NioEventLoopGroup(1);
  public static final Bootstrap DOWN_BOOT = new Bootstrap().group(LOOP_GROUP)
      .channel(NioSocketChannel.class);

  public static final Map<String, HttpDownInfo> DOWN_CONTENT = new ConcurrentHashMap<>();
  public static final Map<String, WebSocketSession> WS_CONTENT = new ConcurrentHashMap<>();

  public static int VIEW_SERVER_PORT;

  @Value("${view.server.port}")
  private int viewServerPort;

  @Override
  public void afterPropertiesSet() throws Exception {
    VIEW_SERVER_PORT = viewServerPort;
  }

  public static void sendMsg(String type, TaskInfo taskInfo) {
    try {
      for (Entry<String, WebSocketSession> entry : HttpDownServer.WS_CONTENT.entrySet()) {
        WebSocketSession session = entry.getValue();
        if (session.isOpen()) {
          Map<String, Object> msg = new HashMap<>();
          msg.put("type", type);
          msg.put("taskInfo", taskInfo);
          TextMessage message = new TextMessage(JSON.toJSONString(msg));
          synchronized (session){
            session.sendMessage(message);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void start(int port) {
    SpringApplication.run(HttpDownServer.class);
    new HttpDownProgressPushTask().start();
    new HttpDownErrorCheckTask().start();
    //监听http下载请求
    new HttpProxyServer().proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new CertDownIntercept());
        pipeline.addLast(new BdyIntercept());
        pipeline.addLast(new HttpDownSniffIntercept());
        pipeline.addLast(new BdyBatchDownIntercept());
        pipeline.addLast(new HttpDownIntercept());
      }
    }).start(port);
  }

  public static void main(String[] args) throws Exception {
    start(9999);
  }
}
