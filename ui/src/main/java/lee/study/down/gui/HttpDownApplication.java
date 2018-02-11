package lee.study.down.gui;

import com.sun.javafx.application.ParametersImpl;
import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
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
import lee.study.down.content.ContentManager;
import lee.study.down.intercept.HttpDownHandleInterceptFactory;
import lee.study.down.mvc.HttpDownSpringBoot;
import lee.study.down.task.HttpDownErrorCheckTask;
import lee.study.down.task.HttpDownProgressEventTask;
import lee.study.down.util.ConfigUtil;
import lee.study.down.util.OsUtil;
import lee.study.down.util.PathUtil;
import lee.study.down.util.WindowsUtil;
import netscape.javascript.JSObject;
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
    System.setProperty("LOG_PATH", PathUtil.ROOT_PATH);
    //netty设置为堆内存分配
    System.setProperty("io.netty.noPreferDirect", "true");
    //不使用内存池
    System.setProperty("io.netty.allocator.numHeapArenas", "0");
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

  private void initHandle() throws Exception {
    //配置文件加载
    initConfig();
    //springboot加载
    List<String> args = ParametersImpl.getParameters(this).getRaw();
    new SpringApplicationBuilder(HttpDownSpringBoot.class).headless(false).build()
        .run(args.toArray(new String[args.size()]));
    //上下文加载
    ContentManager.init();
    //程序退出监听
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (OsUtil.isWindows()) {
        WindowsUtil.disabledProxy();
      }
    }));
  }

  private void beforeOpen() throws Exception {
    //webview加载
    if (ContentManager.CONFIG.get().getUiModel() == 1) {
      initBrowser();
    }

    //证书安装引导
    if (OsUtil.isWindows()) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        if (!WindowsUtil.existsCert(loader.getResourceAsStream("ca.crt"))) {
          if (OsUtil.isAdmin()) { //admin权限静默安装
            WindowsUtil.installCert(loader.getResourceAsStream("ca.crt"));
          } else {
            showMsg("尚未安装证书，点击确定进行安装");
            WindowsUtil.installCert(loader.getResourceAsStream("ca.crt"));
            showMsg("证书安装成功");
          }
        }
      } catch (Exception e) {
        LOGGER.error("install cert error:", e);
      }
    }
  }

  private void afterOpen() {
    //嗅探代理服务器启动
    proxyServer = new HttpDownProxyServer(
        ContentManager.CONFIG.get().getSecProxyConfig(),
        new HttpDownHandleInterceptFactory(httpDownInfo -> Platform.runLater(() -> {
          if (browser != null) {
            String taskId = httpDownInfo.getTaskInfo().getId();
            browser.webEngine.executeScript("vue.$children[0].openTabHandle('/tasks');"
                + "vue.$store.commit('tasks/setNewTaskId','" + taskId + "');"
                + "vue.$store.commit('tasks/setNewTaskStatus',2);");
          }
          open();
        }))
    );
    int sniffProxyPort = ContentManager.CONFIG.get().getProxyPort();
    if (OsUtil.isBusyPort(sniffProxyPort)) {
      showMsg("端口(" + sniffProxyPort + ")被占用，请关闭占用端口的软件或设置新的端口号");
    } else {
      new Thread(() -> proxyServer.start(ContentManager.CONFIG.get().getProxyPort())).start();
    }

    //启动线程
    new HttpDownErrorCheckTask().start();
    new HttpDownProgressEventTask().start();
  }

  @Override
  public void start(Stage stage) throws Exception {
    initHandle();
    this.stage = stage;
    Platform.setImplicitExit(false);
    SwingUtilities.invokeLater(this::addTray);
    stage.setTitle("proxyee-down-" + version);
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
    beforeOpen();
//    open();
//    close();
    afterOpen();
  }

  public void open() {
    if (browser == null || ContentManager.CONFIG.get().getUiModel() == 2) {
      try {
        OsUtil.openBrowse(url);
      } catch (Exception e) {
        LOGGER.error("openBrowse error:", e);
      }
      return;
    }
    if (stage.isShowing()) {
      if (stage.isIconified()) {
        stage.setIconified(false);
      } else {
        stage.toFront();
      }
    } else {
      stage.show();
      stage.toFront();
      stage.setIconified(true);
      stage.setIconified(false);
    }
  }

  public void close() {
    stage.hide();
  }

  private void showMsg(String msg) {
    JOptionPane.showMessageDialog(null, msg, "提示", JOptionPane.WARNING_MESSAGE);
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

        MenuItem crtItem = new MenuItem("下载证书");
        crtItem.addActionListener(event -> {
          try {
            OsUtil
                .openBrowse("http://127.0.0.1:" + ContentManager.CONFIG.get().getProxyPort());
          } catch (Exception e) {
            LOGGER.error("down cert error", e);
          }
        });

        Menu proxyMenu = new Menu("嗅探模式");
        if (!OsUtil.isWindows()) {
          proxyMenu.setEnabled(false);
        } else {
          CheckboxMenuItemGroup mig = new CheckboxMenuItemGroup();
          CheckboxMenuItem globalProxyItem = new CheckboxMenuItem("全网");
          globalProxyItem.setName("1");
          CheckboxMenuItem bdyProxyItem = new CheckboxMenuItem("百度云");
          bdyProxyItem.setName("2");
          CheckboxMenuItem disableProxyItem = new CheckboxMenuItem("关闭");
          disableProxyItem.setName("3");
          proxyMenu.add(globalProxyItem);
          proxyMenu.add(bdyProxyItem);
          proxyMenu.add(disableProxyItem);
          mig.add(globalProxyItem);
          mig.add(bdyProxyItem);
          mig.add(disableProxyItem);
          //默认选中
          if (ContentManager.CONFIG.get().getSniffModel() == 1) {
            mig.selectItem(globalProxyItem);
            WindowsUtil.enabledIEProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
          } else if (ContentManager.CONFIG.get().getSniffModel() == 2) {
            mig.selectItem(bdyProxyItem);
            WindowsUtil.enabledPACProxy(
                "http://127.0.0.1:" + ConfigUtil.getValue("tomcat.server.port")
                    + "/res/pd.pac?t=" + System.currentTimeMillis());
          } else {
            mig.selectItem(disableProxyItem);
            WindowsUtil.disabledProxy();
          }
          mig.addActionListener(event -> {
            try {
              String selectedItemName = ((CheckboxMenuItem) event.getSource()).getName();
              if ("1".equals(selectedItemName)) {
                ContentManager.CONFIG.get().setSniffModel(1);
                WindowsUtil
                    .enabledIEProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
              } else if ("2".equals(selectedItemName)) {
                ContentManager.CONFIG.get().setSniffModel(2);
                WindowsUtil.enabledPACProxy(
                    "http://127.0.0.1:" + ConfigUtil.getValue("tomcat.server.port")
                        + "/res/pd.pac?t=" + System.currentTimeMillis());
              } else {
                ContentManager.CONFIG.get().setSniffModel(3);
                WindowsUtil.disabledProxy();
              }
              ContentManager.CONFIG.save();
            } catch (Exception e) {
              LOGGER.error("proxy switch error", e);
              trayIcon.displayMessage("提示", "嗅探模式切换失败", TrayIcon.MessageType.INFO);
            }
          });
        }

        Menu uiMenu = new Menu("UI模式");
        CheckboxMenuItemGroup mig = new CheckboxMenuItemGroup();
        CheckboxMenuItem guiItem = new CheckboxMenuItem("GUI");
        guiItem.setName("1");
        CheckboxMenuItem browserItem = new CheckboxMenuItem("浏览器");
        browserItem.setName("2");
        uiMenu.add(guiItem);
        uiMenu.add(browserItem);
        mig.add(guiItem);
        mig.add(browserItem);
        //默认选中
        if (ContentManager.CONFIG.get().getUiModel() == 1) {
          mig.selectItem(guiItem);
        } else {
          mig.selectItem(browserItem);
        }
        mig.addActionListener(event -> {
          String selectedItemName = ((CheckboxMenuItem) event.getSource()).getName();
          Platform.runLater(() -> {
            if ("1".equals(selectedItemName)) {
              initBrowser();
              ContentManager.CONFIG.get().setUiModel(1);
            } else {
              destroyBrowser();
              stage.close();
              ContentManager.CONFIG.get().setUiModel(2);
            }
            open();
            ContentManager.CONFIG.save();
          });
        });

        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.addActionListener(event -> Platform.runLater(() -> {
          if (browser != null) {
            browser.webEngine.executeScript("vue.$children[0].openTabHandle('/about');");
          }
          open();
        }));

        MenuItem closeItem = new MenuItem("退出");
        closeItem.addActionListener(event -> System.exit(0));

        popupMenu.add(tasksItem);
        popupMenu.addSeparator();
        popupMenu.add(crtItem);
        popupMenu.add(proxyMenu);
        popupMenu.add(uiMenu);
        popupMenu.addSeparator();
        popupMenu.add(aboutItem);
        popupMenu.add(closeItem);
        // 为托盘图标加弹出菜弹
        trayIcon.setPopupMenu(popupMenu);
        trayIcon.displayMessage("提示", "proxyee-down启动成功", TrayIcon.MessageType.INFO);
      }
    } catch (Exception e) {
      LOGGER.error("addTray error:", e);
      showMsg("托盘初始化失败");
    }
  }

  private void initBrowser() {
    if (this.browser == null) {
      this.browser = new Browser();
      stage.setScene(new Scene(browser));
    }
    browser.load(this.url);
  }

  private void destroyBrowser() {
    if (this.browser != null) {
      browser.load(null);
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
      webEngine.getLoadWorker().stateProperty().addListener(
          (ObservableValue<? extends State> ov, State oldState,
              State newState) -> {
            if (newState == State.SUCCEEDED) {
              JSObject win = (JSObject) webEngine.executeScript("window");
              win.setMember("jNative", new JNative());
            }
          });
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

  public class JNative {

    public void open(String url) {
      try {
        OsUtil.openBrowse(url);
      } catch (Exception e) {
        LOGGER.warn("can't openBrowse:", e);
      }
    }

  }

  public static void main(String[] args) {
    launch(args);
  }
}