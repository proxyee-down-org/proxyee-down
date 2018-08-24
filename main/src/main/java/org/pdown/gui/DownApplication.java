package org.pdown.gui;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.com.Browser;
import org.pdown.gui.com.Components;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.mitm.server.PDownProxyServer;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.http.EmbedHttpServer;
import org.pdown.gui.http.controller.NativeController;
import org.pdown.gui.http.controller.PacController;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.gui.util.ExecUtil;
import org.pdown.gui.util.I18nUtil;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.RestWebServerFactoryCustomizer;
import org.pdown.rest.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.pdown.rest")
public class DownApplication extends AbstractJavaFxApplicationSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(DownApplication.class);

  private static final String OS = OsUtil.isWindows() ? "windows"
      : (OsUtil.isMac() ? "mac" : "linux");
  private static final String ICON_NAME = OS + "/logo.png";

  private Stage stage;
  private Browser browser;
  private TrayIcon trayIcon;

  private CountDownLatch countDownLatch;
  //前端页面http服务器端口
  public static int FRONT_PORT;
  //native api服务器端口
  public static int API_PORT;

  @Override
  public void start(Stage primaryStage) throws Exception {
    stage = primaryStage;
    Platform.setImplicitExit(false);
    //load config
    initConfig();
    initMacTool();
    initEmbedHttpServer();
    initExtension();
    initTray();
    initWindow();
    initBrowser();
    show();
  }


  private void initConfig() throws IOException {
    PDownConfigContent.getInstance().load();
    //取前端http server端口
    FRONT_PORT = ConfigUtil.getInt("front.port");
    //取native api http server端口
    API_PORT = ConfigUtil.getInt("api.port");
    if ("prd".equals(ConfigUtil.getString("spring.profiles.active"))) {
      try {
        //打包的情况随机使用一个端口
        FRONT_PORT = OsUtil.getFreePort(FRONT_PORT);
        API_PORT = FRONT_PORT;
      } catch (IOException e) {
        LOGGER.error("initConfig error", e);
        alertAndExit(I18nUtil.getMessage("gui.alert.startError", e.getMessage()));
      }
    }
  }

  public static int proxyPort;

  //读取扩展信息和启动代理服务器
  private void initExtension() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      //退出时把系统代理还原
      if (PDownConfigContent.getInstance().get().getProxyMode() == 1) {
        try {
          ExtensionProxyUtil.disabledProxy();
        } catch (IOException e) {
        }
      }
    }));
    new Thread(() -> {
      try {
        proxyPort = OsUtil.getFreePort(9999);
        PDownProxyServer.start(proxyPort);
      } catch (Exception e) {
        LOGGER.error("Init extension error", e);
        alertAndExit("Init extension error：" + e.getMessage());
      }
    }).start();
    try {
      ExtensionContent.load();
      //切换系统pac代理
      if (PDownConfigContent.getInstance().get().getProxyMode() == 1) {
        ExtensionProxyUtil.enabledPACProxy("http://127.0.0.1:" + API_PORT + "/pac/pdown.pac?t=" + System.currentTimeMillis());
      }
    } catch (IOException e) {
      LOGGER.error("Extension content load error", e);
    }
  }

  public static int macToolPort;

  //加载mac tool
  private void initMacTool() {
    if (OsUtil.isMac()) {
      new Thread(() -> {
        String toolUri = "tool/mac-tool";
        Path toolPath = Paths.get(PathUtil.ROOT_PATH + File.separator + toolUri);
        try {
          if (!toolPath.toFile().exists()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(toolUri);
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
              if (!toolPath.getParent().toFile().exists()) {
                Files.createDirectories(toolPath.getParent());
              }
              Files.copy(classLoader.getResourceAsStream(toolUri), toolPath);
              Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrw-rw-");
              Files.setPosixFilePermissions(toolPath, perms);
            }
          }
          //取一个空闲端口来运行mac tool
          macToolPort = OsUtil.getFreePort(9393);
          //程序退出监听
          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
              ExecUtil.httpGet("http://127.0.0.1:" + macToolPort + "/quit");
            } catch (IOException e) {
            }
          }));
          ExecUtil.execSyncWithAdmin("'" + toolPath.toFile().getPath() + "' " + macToolPort);
        } catch (Exception e) {
          LOGGER.error("initMacTool error", e);
          alertAndExit("initMacTool error：" + e.getMessage());
        }
        System.exit(0);
      }).start();
    }
  }

  private void initEmbedHttpServer() {
    countDownLatch = new CountDownLatch(1);
    new Thread(() -> {
      EmbedHttpServer embedHttpServer = new EmbedHttpServer(API_PORT);
      embedHttpServer.addController(new NativeController());
      embedHttpServer.addController(new PacController());
      embedHttpServer.start(future -> countDownLatch.countDown());
    }).start();
  }

  //加载托盘
  private void initTray() throws AWTException {
    if (SystemTray.isSupported()) {
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      // 获取图片所在的URL
      URL url = Thread.currentThread().getContextClassLoader().getResource(ICON_NAME);
      // 为系统托盘加托盘图标
      Image trayImage = Toolkit.getDefaultToolkit().getImage(url);
      Dimension trayIconSize = systemTray.getTrayIconSize();
      trayImage = trayImage.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
      trayIcon = new TrayIcon(trayImage, "Proxyee Down");
      systemTray.add(trayIcon);

      //双击事件监听
      trayIcon.addActionListener(event -> Platform.runLater(() -> show()));

      //添加右键菜单
      PopupMenu popupMenu = new PopupMenu();
      MenuItem showItem = new MenuItem(I18nUtil.getMessage("gui.tray.show"));
      showItem.addActionListener(event -> Platform.runLater(() -> show()));
      MenuItem closeItem = new MenuItem(I18nUtil.getMessage("gui.tray.exit"));
      closeItem.addActionListener(event -> {
        Platform.exit();
        System.exit(0);
      });
      popupMenu.add(showItem);
      popupMenu.add(closeItem);
      trayIcon.setPopupMenu(popupMenu);
    }
  }

  //加载webView
  private void initBrowser() throws AWTException {
    browser = new Browser();
    stage.setScene(new Scene(browser));
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
    }
    browser.load("http://127.0.0.1:" + FRONT_PORT);
  }

  //加载gui窗口
  private void initWindow() {
    stage.setTitle("Proxyee Down");
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());
    stage.getIcons().add(new javafx.scene.image.Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(ICON_NAME)));
    //关闭窗口监听
    stage.setOnCloseRequest(event -> {
      event.consume();
      stage.hide();
    });
  }

  //显示gui窗口
  private void show() {
    if (stage.isShowing()) {
      if (stage.isIconified()) {
        stage.setIconified(false);
      } else {
        stage.toFront();
      }
    } else {
      stage.show();
      stage.toFront();
    }
    /*//避免有时候窗口不弹出
    if (isFront && !isTray && OsUtil.isWindows()) {
      stage.setIconified(true);
      stage.setIconified(false);
    }*/
  }

  //提示并退出程序
  private void alertAndExit(String msg) {
    Platform.runLater(() -> {
      Components.alert(msg);
      System.exit(0);
    });
  }

  static {
    //设置日志存放路径
    System.setProperty("ROOT_PATH", PathUtil.ROOT_PATH);
    //webView允许跨域访问
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    //处理MAC dock图标
    if (OsUtil.isMac()) {
      try {
        Class<?> appClass = Class.forName("com.apple.eawt.Application");
        Method getApplication = appClass.getMethod("getApplication");
        Object application = getApplication.invoke(appClass);
        Method setDockIconImage = appClass.getMethod("setDockIconImage", Image.class);
        URL url = Thread.currentThread().getContextClassLoader().getResource("mac/dock_logo.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        setDockIconImage.invoke(application, image);
      } catch (Exception e) {
        LOGGER.error("handle mac dock icon error", e);
      }
    }
  }

  private static final int REST_PORT = 26339;

  private static void doCheck() {
    if (OsUtil.isBusyPort(REST_PORT)) {
      JOptionPane.showMessageDialog(
          null,
          I18nUtil.getMessage("gui.alert.startError", I18nUtil.getMessage("gui.alert.restPortBusy")),
          I18nUtil.getMessage("gui.warning"),
          JOptionPane.WARNING_MESSAGE);
      System.exit(0);
    }
  }

  public static void main(String[] args) {
    //init rest server config
    RestWebServerFactoryCustomizer.init(null);
    ConfigContent.getInstance().get().setPort(REST_PORT);
    //get free port
    doCheck();
    launch(DownApplication.class, null, args);
  }
}
