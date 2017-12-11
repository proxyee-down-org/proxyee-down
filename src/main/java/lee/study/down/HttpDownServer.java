package lee.study.down;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.down.dispatch.HttpDownErrorCheckTask;
import lee.study.down.dispatch.HttpDownProgressEventTask;
import lee.study.down.dispatch.HttpDownStartCallback;
import lee.study.down.intercept.BdyBatchDownIntercept;
import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.RecordInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationHome;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer implements InitializingBean {

  public static final NioEventLoopGroup LOOP_GROUP = new NioEventLoopGroup(1);
  public static final Bootstrap DOWN_BOOT = new Bootstrap().group(LOOP_GROUP)
      .channel(NioSocketChannel.class);
  public static final String HOME_PATH = new ApplicationHome(HttpDownServer.class).getDir()
      .getPath();
  public static final String RECORD_PATH = HOME_PATH + File.separator + "records.inf";
  public static final Map<String, WebSocketSession> WS_CONTENT = new ConcurrentHashMap<>();
  public static final Map<String, HttpDownInfo> DOWN_CONTENT = new ConcurrentHashMap<>();

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
          synchronized (session) {
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

    //读取之前的下载信息
    File file = new File(RECORD_PATH);
    if (file.exists()) {
      try (
          FileInputStream inputStream = new FileInputStream(file);
      ) {
        byte[] head = new byte[4];
        while (inputStream.read(head) != -1) {
          byte[] body = new byte[ByteUtil.btsToInt(head)];
          inputStream.read(body);
          RecordInfo recordInfo = (RecordInfo) ByteUtil.deserialize(body);
          HttpDownInfo httpDownInfo = (HttpDownInfo) ByteUtil.deserialize(
              recordInfo.getFilePath() + File.separator + recordInfo.getFileName() + ".inf");
          TaskInfo taskInfo = httpDownInfo.getTaskInfo();
          taskInfo.setCallback(new HttpDownStartCallback());
          //全部标记为失败
          taskInfo.getChunkInfoList().forEach(chunk -> chunk.setStatus(0));
          DOWN_CONTENT.put(taskInfo.getId(), httpDownInfo);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    new HttpDownProgressEventTask().start();
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
    start(args.length == 0 ? 9999 : Integer.parseInt(args[0]));
  }
}
