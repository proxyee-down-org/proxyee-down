package org.pdown.gui;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import java.awt.AWTException;
import java.awt.Desktop;
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
import java.net.URI;
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
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.http.EmbedHttpServer;
import org.pdown.gui.http.controller.ApiController;
import org.pdown.gui.http.controller.NativeController;
import org.pdown.gui.http.controller.PacController;
import org.pdown.gui.util.AppUtil;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.gui.util.ExecUtil;
import org.pdown.gui.util.I18nUtil;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.RestWebServerFactoryCustomizer;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

@SpringBootApplication
@ComponentScan(basePackages = "org.pdown.rest")
public class DownApplication extends AbstractJavaFxApplicationSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(DownApplication.class);

  private static final String OS = OsUtil.isWindows() ? "windows"
      : (OsUtil.isMac() ? "mac" : "linux");
  private static final String ICON_NAME = OS + "/logo.png";

  public static DownApplication INSTANCE;

  private Stage stage;
  private Browser browser;
  private TrayIcon trayIcon;

  private CountDownLatch countDownLatch;
  //前端页面http服务器端口
  public int FRONT_PORT;
  //native api服务器端口
  public int API_PORT;
  //代理服务器端口
  public int PROXY_PORT;

  @Override
  public void start(Stage primaryStage) throws Exception {
    INSTANCE = this;
    stage = primaryStage;
    Platform.setImplicitExit(false);
    //load config
    initConfig();
    initMacMITMTool();
    initEmbedHttpServer();
    initExtension();
    initTray();
    initWindow();
    initBrowser();
    loadUri(null, true);
  }


  private void initConfig() throws IOException {
    PDownConfigContent.getInstance().load();
    //取前端http server端口
    FRONT_PORT = ConfigUtil.getInt("front.port");
    //取native api http server端口
    API_PORT = ConfigUtil.getInt("api.port");
    if ("prd".equals(ConfigUtil.getString("spring.profiles.active"))) {
      try {
        //端口被时占用随机分配一个端口
        API_PORT = OsUtil.getFreePort(API_PORT);
        if (FRONT_PORT == -1) {
          FRONT_PORT = API_PORT;
        }
      } catch (IOException e) {
        LOGGER.error("initConfig error", e);
        alertAndExit(I18nUtil.getMessage("gui.alert.startError", e.getMessage()));
      }
    }
  }

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
      //检查是否安装了证书
      try {
        if (AppUtil.checkIsInstalledCert()) {
          AppUtil.startProxyServer();
        }
      } catch (Exception e) {
        LOGGER.error("Init extension error", e);
      }
    }).start();
    //根据扩展生成pac文件并切换系统代理
    try {
      ExtensionContent.load();
      AppUtil.refreshPAC();
    } catch (IOException e) {
      LOGGER.error("Extension content load error", e);
    }
  }

  public static int macToolPort;

  //加载mac tool
  private void initMacMITMTool() {
    if (OsUtil.isMac()) {
      new Thread(() -> {
        String toolUri = "mac/mitm-tool.bin";
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
          macToolPort = OsUtil.getFreePort();
          //程序退出监听
          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
              ExecUtil.httpGet("http://127.0.0.1:" + macToolPort + "/quit");
            } catch (IOException e) {
            }
          }));
          ExecUtil.execBlockWithAdmin("'" + toolPath.toFile().getPath() + "' " + macToolPort);
        } catch (Exception e) {
          LOGGER.error("initMacMITMTool error", e);
          alertAndExit("Init mitm-tool error：" + e.getMessage());
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
      embedHttpServer.addController(new ApiController());
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
      loadPopupMenu();
      //双击事件监听
      trayIcon.addActionListener(event -> Platform.runLater(() -> loadUri(null, true)));
    }
  }

  public void loadPopupMenu() {
    //添加右键菜单
    PopupMenu popupMenu = new PopupMenu();
    MenuItem showItem = new MenuItem(I18nUtil.getMessage("gui.tray.show"));
    showItem.addActionListener(event -> Platform.runLater(() -> loadUri("", true)));
    MenuItem setItem = new MenuItem(I18nUtil.getMessage("gui.tray.set"));
    setItem.addActionListener(event -> loadUri("/#/setting", true));
    MenuItem aboutItem = new MenuItem(I18nUtil.getMessage("gui.tray.about"));
    aboutItem.addActionListener(event -> loadUri("/#/about", true));
    MenuItem supportItem = new MenuItem(I18nUtil.getMessage("gui.tray.support"));
    supportItem.addActionListener(event -> loadUri("/#/support", true));
    MenuItem closeItem = new MenuItem(I18nUtil.getMessage("gui.tray.exit"));
    closeItem.addActionListener(event -> {
      Platform.exit();
      System.exit(0);
    });
    popupMenu.add(showItem);
    popupMenu.addSeparator();
    popupMenu.add(setItem);
    popupMenu.add(aboutItem);
    popupMenu.add(supportItem);
    popupMenu.addSeparator();
    popupMenu.add(closeItem);
    trayIcon.setPopupMenu(popupMenu);
  }

  //加载webView
  private void initBrowser() throws AWTException {
    browser = new Browser();
    stage.setScene(new Scene(browser));
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
    }
  }

  //加载gui窗口
  private void initWindow() {
    stage.setTitle("Proxyee Down");
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    int width = 1024;
    int height = 576;
    stage.setX((bounds.getWidth() - width) / 2);
    stage.setY((bounds.getHeight() - height) / 2);
    stage.setMinWidth(width);
    stage.setMinHeight(height);
    stage.getIcons().add(new javafx.scene.image.Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(ICON_NAME)));
    stage.setResizable(true);
    //关闭窗口监听
    stage.setOnCloseRequest(event -> {
      event.consume();
      stage.hide();
    });
  }

  /**
   * 显示gui窗口
   *
   * @param isTray 是否从托盘按钮打开的(windows下如果非托盘按钮调用窗口可能不会置顶)
   */
  public void show(boolean isTray) {
    //是否需要调用窗口置顶
    boolean isFront = false;
    if (stage.isShowing()) {
      if (stage.isIconified()) {
        stage.setIconified(false);
      } else {
        isFront = true;
        stage.toFront();
      }
    } else {
      isFront = true;
      stage.show();
      stage.toFront();
    }
    //避免有时候窗口不弹出
    if (isFront && !isTray && OsUtil.isWindows()) {
      stage.setIconified(true);
      stage.setIconified(false);
    }
  }

  public void loadUri(String uri, boolean isTray) {
    String url = "http://127.0.0.1:" + FRONT_PORT + (uri == null ? "" : uri);
    if (PDownConfigContent.getInstance().get().getUiMode() == 0) {
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (IOException e) {
        LOGGER.error("Open browse error", e);
      }
    } else {
      Platform.runLater(() -> {
        if (uri != null || !browser.isLoad()) {
          browser.load(url);
        }
        show(isTray);
      });
    }
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

  //-Dio.netty.leakDetection.level=PARANOID
  //https://stackoverflow.com/questions/39192528/how-can-you-send-information-to-the-windows-task-bar-from-java-o-javafx
  public static void main(String[] args) {
    //init rest server config
    RestWebServerFactoryCustomizer.init(null);
    ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
    serverConfigInfo.setPort(REST_PORT);
    if (StringUtils.isEmpty(serverConfigInfo.getFilePath())) {
      serverConfigInfo.setFilePath(System.getProperty("user.home") + File.separator + "Downloads");
    }
    //get free port
    doCheck();
    launch(DownApplication.class, null, args);
  }
}
