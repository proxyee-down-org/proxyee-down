package lee.study.down;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
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
import lee.study.down.model.TaskBaseInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.proxyee.exception.HttpProxyExceptionHandle;
import lee.study.proxyee.intercept.CertDownIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.beans.BeanUtils;
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
  public static final Map<String, TaskBaseInfo> RECORD_CONTENT = new ConcurrentHashMap<>();
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
      try {
        RECORD_CONTENT.putAll((Map) ByteUtil.deserialize(RECORD_PATH));
        for (Entry<String, TaskBaseInfo> entry : RECORD_CONTENT.entrySet()) {
          HttpDownInfo httpDownInfo;
          TaskBaseInfo taskBaseInfo = entry.getValue();
          File taskInfoFile = new File(
              taskBaseInfo.getFilePath() + File.separator + taskBaseInfo.getFileName() + ".inf");
          if (taskInfoFile.exists()) {
            //下载中的还原之前的状态
            httpDownInfo = (HttpDownInfo) ByteUtil.deserialize(taskInfoFile.getPath());
            TaskInfo taskInfo = httpDownInfo.getTaskInfo();
            taskInfo.setCallback(new HttpDownStartCallback());
            //全部标记为失败,等待重新下载
            taskInfo.getChunkInfoList().forEach(chunk -> chunk.setStatus(3));
          } else {
            //下载完成的
            TaskInfo temp = new TaskInfo();
            BeanUtils.copyProperties(taskBaseInfo, temp);
            httpDownInfo = new HttpDownInfo(temp, null);
          }
          DOWN_CONTENT.put(taskBaseInfo.getId(), httpDownInfo);
        }
      } catch (Exception e) {
        System.out.println("加载配置文件失败：" + e.getMessage());
      }
    }

    new HttpDownProgressEventTask().start();
    new HttpDownErrorCheckTask().start();
    //监听http下载请求
    new HttpProxyServer()
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new CertDownIntercept());
            pipeline.addLast(new BdyIntercept());
            pipeline.addLast(new HttpDownSniffIntercept());
            pipeline.addLast(new BdyBatchDownIntercept());
            pipeline.addLast(new HttpDownIntercept());
          }
        })
        .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
          @Override
          public void beforeCatch(Channel clientChannel, Throwable cause) {
            if (cause instanceof ConnectException) {
              System.out.println("连接超时:" + cause.toString());
            } else if (cause instanceof IOException) {
              System.out.println("IO异常:" + cause.toString());
            } else {
              cause.printStackTrace();
            }
          }

          @Override
          public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause) {
            beforeCatch(clientChannel, cause);
          }
        })
        .start(port);
  }

  public static void main(String[] args) throws Exception {
    start(args.length == 0 ? 9999 : Integer.parseInt(args[0]));
  }
}
