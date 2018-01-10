package lee.study.down;

import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.HttpDownContent;
import lee.study.down.gui.HttpDownTray;
import lee.study.down.intercept.HttpDownHandleInterceptFactory;
import lee.study.down.task.HttpDownErrorCheckTask;
import lee.study.down.task.HttpDownProgressEventTask;
import lee.study.down.util.OsUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class HttpDownApplication implements InitializingBean, EmbeddedServletContainerCustomizer {

  static {
    //设置slf4j日志打印目录
    System.setProperty("LOG_PATH", HttpDownConstant.HOME_PATH);
  }

  public static void main(String[] args) throws Exception {
    //启动前端页面web server
    ApplicationContext context = new SpringApplicationBuilder(HttpDownApplication.class)
        .headless(false).build().run(args);
    //获取application实例
    HttpDownApplication application = context.getBean(HttpDownApplication.class);
    //代理服务器启动
    Thread proxyServerThread = new Thread(() -> application.httpDownProxyServer.start());
    proxyServerThread.setDaemon(true);
    proxyServerThread.start();
    //托盘初始化
    application.httpDownTray.init();
    //任务加载
    HttpDownContent.init();
    //打开浏览器访问前端页面
    OsUtil.openBrowse(application.homeUrl);
    //启动线程
    new HttpDownErrorCheckTask().start();
    new HttpDownProgressEventTask().start();
  }

  @Value("${spring.profiles.active}")
  private String active;

  @Value("${view.server.port}")
  private int viewServerPort;

  @Value("${tomcat.server.port}")
  private int tomcatServerPort;

  private String homeUrl;
  private HttpDownTray httpDownTray;
  private HttpDownProxyServer httpDownProxyServer;

  @Override
  public void afterPropertiesSet() throws Exception {
    boolean isDev = false;
    if ("dev".equalsIgnoreCase(active.trim())) {
      isDev = true;
    } else {
      viewServerPort = tomcatServerPort = OsUtil.getFreePort(tomcatServerPort);
    }
    //node.js代理中间件需用localhost访问，不然有时候会出现请求无响应的问题
    homeUrl = "http://" + (isDev ? "localhost" : "127.0.0.1") + ":" + viewServerPort;
    httpDownProxyServer = new HttpDownProxyServer(9999,
        new HttpDownHandleInterceptFactory(isDev, viewServerPort));
    httpDownTray = new HttpDownTray(homeUrl);
  }

  @Override
  public void customize(ConfigurableEmbeddedServletContainer container) {
    container.setPort(tomcatServerPort);
  }

}
