package lee.study.down.gui;

import com.sun.javafx.application.ParametersImpl;
import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lee.study.down.HttpDownProxyServer;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
import lee.study.down.content.WsContent;
import lee.study.down.intercept.HttpDownHandleInterceptFactory;
import lee.study.down.mvc.HttpDownSpringBoot;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;
import lee.study.down.task.HttpDownErrorCheckTask;
import lee.study.down.task.HttpDownProgressEventTask;
import lee.study.down.util.ConfigUtil;
import lee.study.down.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class HttpDownApplication extends Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownApplication.class);

  private String url;
  private float version;
  private Stage stage;
  private Browser browser;

  private static HttpDownProxyServer proxyServer;

  static {
    //设置slf4j日志打印目录
    System.setProperty("LOG_PATH", HttpDownConstant.HOME_PATH);
  }

  private void initConfig() throws Exception {
    int viewPort = Integer.parseInt(ConfigUtil.getValue("view.server.port"));
    int tomcatPort = Integer.parseInt(ConfigUtil.getValue("tomcat.server.port"));
    if ("prd".equalsIgnoreCase(ConfigUtil.getValue("spring.profiles.active"))) {
      if (OsUtil.isBusyPort(tomcatPort)) {
        tomcatPort = OsUtil.getFreePort(tomcatPort + 1);
      }
      viewPort = tomcatPort;
      ConfigUtil.setValue("view.server.port", viewPort);
      ConfigUtil.setValue("tomcat.server.port", tomcatPort);
    }
    this.url = "http://127.0.0.1:" + viewPort;
    this.version = Float.parseFloat(ConfigUtil.getValue("app.version"));
  }

  @Override
  public void start(Stage stage) throws Exception {
    initConfig();
    ContentManager.init();
    this.stage = stage;
    this.browser = new Browser();

    Platform.setImplicitExit(false);
    SwingUtilities.invokeLater(this::addTray);
    stage.setTitle("proxyee-down-" + version);
    stage.setResizable(false);
    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    stage.setX(primaryScreenBounds.getMinX());
    stage.setY(primaryScreenBounds.getMinY());
    stage.setWidth(primaryScreenBounds.getWidth());
    stage.setHeight(primaryScreenBounds.getHeight());
    stage.getIcons().add(new Image(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("favicon.png")));

    stage.setOnCloseRequest(event -> {
      event.consume();
      close();
    });

    List<String> args = ParametersImpl.getParameters(this).getRaw();
    //springboot加载
    new SpringApplicationBuilder(HttpDownSpringBoot.class).headless(false).build()
        .run(args.toArray(new String[args.size()]));

    //webview加载
    stage.setScene(new Scene(browser));
    browser.load(this.url);

    //嗅探代理服务器启动
    proxyServer = new HttpDownProxyServer(
        ContentManager.CONFIG.get().getSecProxyConfig(),
        new HttpDownHandleInterceptFactory(httpDownInfo -> Platform.runLater(() -> {
          ContentManager.WS
              .sendMsg(new WsForm(WsDataType.NEW_TASK, httpDownInfo.getTaskInfo().getId()));
          open();
        }))
    );
    int sniffProxyPort = ContentManager.CONFIG.get().getProxyPort();
    if (OsUtil.isBusyPort(sniffProxyPort)) {
      showMsg("端口(" + sniffProxyPort + ")被占用，请关闭占用端口的软件或设置新的端口号");
    } else {
      new Thread(() -> proxyServer.start(ContentManager.CONFIG.get().getProxyPort())).start();
    }

    open();

    //启动线程
    new HttpDownErrorCheckTask().start();
    new HttpDownProgressEventTask().start();

  }

  public void open() {
    if ("dev".equalsIgnoreCase(ConfigUtil.getValue("spring.profiles.active"))) {
      try {
        OsUtil.openBrowse(url);
      } catch (Exception e) {
        LOGGER.error("openBrowse error:", e);
      }
      return;
    }
    if (stage.isShowing()) {
      stage.setIconified(true);
      stage.setIconified(false);
    } else {
      stage.show();
      stage.toFront();
    }
  }

  public void open(String uri) {
    browser.load(this.url + uri);
    open();
  }

  public void close() {
    stage.hide();
  }

  private void showMsg(String msg) {
    JOptionPane.showMessageDialog(null, msg, "运行警告", JOptionPane.WARNING_MESSAGE);
  }

  private void addTray() {
    try {
      if (SystemTray.isSupported()) {
        // 获得系统托盘对象
        SystemTray systemTray = SystemTray.getSystemTray();
        // 获取图片所在的URL
        URL url = Thread.currentThread().getContextClassLoader().getResource("favicon.png");
        TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(url), "proxyee-down");
        // 为系统托盘加托盘图标
        systemTray.add(trayIcon);
        trayIcon.setImageAutoSize(true);
        // 托盘双击事件
        trayIcon.addActionListener(event -> Platform.runLater(() -> open()));
        // 创建弹出菜单
        PopupMenu popupMenu = new PopupMenu();
        MenuItem tasksItem = new MenuItem("显示");
        tasksItem.addActionListener(event -> Platform.runLater(() -> open()));

        MenuItem crtItem = new MenuItem("安装证书");
        crtItem.addActionListener(event -> {
          try {
            URL crtUrl = new URL(
                "http://127.0.0.1:" + ContentManager.CONFIG.get().getProxyPort() + "/ca.crt");
            URLConnection connection = crtUrl.openConnection();
            OsUtil.execFile(connection.getInputStream(),
                HttpDownConstant.HOME_PATH + File.separator + "ca.crt");
          } catch (Exception e) {
            LOGGER.error("install crt error:", e);
            trayIcon.displayMessage("提示", "证书安装失败", TrayIcon.MessageType.INFO);
          }
        });

        Menu proxyMenu = new Menu("全局代理");
        if (!OsUtil.isWindows()) {
          proxyMenu.setEnabled(false);
        } else {
          CheckboxMenuItemGroup mig = new CheckboxMenuItemGroup();
          CheckboxMenuItem enableProxyItem = new CheckboxMenuItem("启用");
          CheckboxMenuItem disableProxyItem = new CheckboxMenuItem("关闭");
          proxyMenu.add(enableProxyItem);
          proxyMenu.add(disableProxyItem);
          mig.add(enableProxyItem);
          mig.add(disableProxyItem);
          //默认选中
          if (ContentManager.CONFIG.get().getProxyModel() == 1) {
            mig.selectItem(enableProxyItem);
            OsUtil.enabledIEProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
          } else {
            mig.selectItem(disableProxyItem);
            OsUtil.disabledIEProxy();
          }
          mig.addActionListener(event -> {
            try {
              if ("启用".equals(event.getItem())) {
                ContentManager.CONFIG.get().setProxyModel(1);
                OsUtil.enabledIEProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
              } else {
                ContentManager.CONFIG.get().setProxyModel(0);
                OsUtil.disabledIEProxy();
              }
              ContentManager.CONFIG.save();
            } catch (Exception e) {
              LOGGER.error("proxy switch error", e);
              trayIcon.displayMessage("提示", "全局代理设置失败", TrayIcon.MessageType.INFO);
            }
          });
        }

        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.addActionListener(event -> Platform.runLater(() -> open("/#/about")));

        MenuItem closeItem = new MenuItem("退出");
        closeItem.addActionListener(event -> {
          try {
            OsUtil.disabledIEProxy();
          } catch (IOException e) {
          }
          System.exit(1);
        });

        popupMenu.add(tasksItem);
        popupMenu.addSeparator();
        popupMenu.add(crtItem);
        popupMenu.add(proxyMenu);
        popupMenu.addSeparator();
        popupMenu.add(aboutItem);
        popupMenu.add(closeItem);
        // 为托盘图标加弹出菜弹
        trayIcon.setPopupMenu(popupMenu);
        trayIcon.displayMessage("提示", "软件启动成功", TrayIcon.MessageType.INFO);
      }
    } catch (Exception e) {
      LOGGER.error("addTray error:", e);
      showMsg("托盘初始化失败");
    }
  }

  public static HttpDownProxyServer getProxyServer() {
    return proxyServer;
  }

  class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser() {
      getChildren().add(browser);
    }

    @Override
    protected void layoutChildren() {
      double w = getWidth();
      double h = getHeight();
      layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    public void load(String url) {
      webEngine.load(url);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}