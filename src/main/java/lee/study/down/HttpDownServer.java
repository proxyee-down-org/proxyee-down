package lee.study.down;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFrame;
import lee.study.down.config.ConfigInfo;
import lee.study.down.dispatch.DefaultHttpDownCallback;
import lee.study.down.dispatch.HttpDownErrorCheckTask;
import lee.study.down.dispatch.HttpDownProgressEventTask;
import lee.study.down.intercept.BdyBatchDownIntercept;
import lee.study.down.intercept.BdyIntercept;
import lee.study.down.intercept.HttpDownIntercept;
import lee.study.down.intercept.HttpDownSniffIntercept;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lee.study.down.util.OsUtil;
import lee.study.down.window.DownTray;
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
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
public class HttpDownServer implements InitializingBean, EmbeddedServletContainerCustomizer {

  public static final NioEventLoopGroup LOOP_GROUP = new NioEventLoopGroup();
  public static final Bootstrap DOWN_BOOT = new Bootstrap().group(LOOP_GROUP)
      .channel(NioSocketChannel.class);
  public static final String HOME_PATH =
      System.getProperty("appdir") != null ? System.getProperty("appdir") //exe4j打包
          : new ApplicationHome(HttpDownServer.class).getDir()
              .getPath();
  public static final String RECORD_PATH = HOME_PATH + File.separator + "records.inf";
  public static final String CONFIG_PATH = HOME_PATH + File.separator + "proxyee-down.cfg";
  public static final Map<String, WebSocketSession> WS_CONTENT = new ConcurrentHashMap<>();
  public static final Map<String, HttpDownInfo> DOWN_CONTENT = new ConcurrentHashMap<>();

  public static ConfigInfo CONFIG_INFO;
  public static Map<String, TaskInfo> RECORD_CONTENT;

  public static int VIEW_SERVER_PORT;
  public static SslContext CLIENT_SSL_CONTEXT;
  public static HttpProxyServer PROXY_SERVER;

  @Value("${view.server.port}")
  private int viewServerPort;

  @Value("${tomcat.server.port}")
  private int tomcatServerPort;

  @Override
  public void afterPropertiesSet() throws Exception {
    VIEW_SERVER_PORT = viewServerPort == -1 ? OsUtil.getFreePort() : viewServerPort;
  }

  public static void start() {
    new SpringApplicationBuilder(HttpDownServer.class)
        .headless(false).run();
    new DownTray();
    loadConfig();
    loadRecord();

    PROXY_SERVER = new HttpProxyServer();
    CLIENT_SSL_CONTEXT = PROXY_SERVER.getClientSslContext();

    new HttpDownProgressEventTask().start();
    new HttpDownErrorCheckTask().start();

    //监听http下载请求
    PROXY_SERVER.proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new CertDownIntercept());
//        pipeline.addLast(new VideoSniffIntercept());
        pipeline.addLast(new BdyIntercept());
        pipeline.addLast(new HttpDownSniffIntercept());
//        pipeline.addLast(new BdyBatchDownIntercept());
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
        .start(CONFIG_INFO.getLocalPort());
  }

  public static void main(String[] args) throws Exception {
    start();
  }

  private static void loadRecord() {
    //读取之前的下载信息
    File file = new File(RECORD_PATH);
    if (file.exists()) {
      try {
        RECORD_CONTENT = ((Map) ByteUtil.deserialize(RECORD_PATH));
        for (Entry<String, TaskInfo> entry : RECORD_CONTENT.entrySet()) {
          HttpDownInfo httpDownInfo = null;
          TaskInfo taskBaseInfo = entry.getValue();
          if (taskBaseInfo.getStatus() == 0) {
            RECORD_CONTENT.remove(entry.getKey());
            continue;
          }
          File taskInfoFile = new File(taskBaseInfo.buildTaskFilePath() + ".inf");
          boolean isDowned = true;
          if (taskInfoFile.exists()) {
            //下载中的还原之前的状态
            httpDownInfo = (HttpDownInfo) ByteUtil.deserialize(taskInfoFile.getPath());
            //是同一个任务
            if (httpDownInfo.getTaskInfo().getId().equals(taskBaseInfo.getId())) {
              //标记为待下载
              isDowned = false;
              //全部标记为暂停,等待重新下载
              TaskInfo taskInfo = httpDownInfo.getTaskInfo();
              taskInfo.setCallback(new DefaultHttpDownCallback());
              if (taskInfo.getStatus() == 5) {  //合并状态检查临时文件夹是否还存在
                if (FileUtil.getFileSize(taskInfo.buildChunksPath()) != taskInfo.getTotalSize()) {
                  taskInfo.setStatus(1);
                  taskInfo.getChunkInfoList().forEach(chunk -> {//重新下载
                    chunk.setStatus(4);
                  });
                }
              } else {
                taskInfo.getChunkInfoList().forEach(chunk -> {//非合并状态需要下载
                  if (chunk.getStatus() != 2) {
                    chunk.setStatus(4);
                  }
                });
              }
            } else {
              RECORD_CONTENT.remove(entry.getKey());
              continue;
            }
          }
          if (isDowned) {
            //下载完成的
//            taskBaseInfo.setStatus(2);
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
    if(RECORD_CONTENT==null){
      RECORD_CONTENT = new ConcurrentHashMap<>();
    }
  }

  private static void loadConfig() {
    File file = new File(CONFIG_PATH);
    if (file.exists()) {
      try {
        CONFIG_INFO = (ConfigInfo) ByteUtil.deserialize(CONFIG_PATH);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if(CONFIG_INFO ==null){
      CONFIG_INFO = new ConfigInfo();
      CONFIG_INFO.setFirst(true);
      CONFIG_INFO.setLocalPort(9999);
      CONFIG_INFO.setLocalProxyType(1);
      try {
        ByteUtil.serialize(CONFIG_INFO,CONFIG_PATH);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
    configurableEmbeddedServletContainer.setPort(tomcatServerPort==-1?VIEW_SERVER_PORT:tomcatServerPort);
  }
}
