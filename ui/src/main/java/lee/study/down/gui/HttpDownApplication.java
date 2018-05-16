package lee.study.down.gui;

import com.sun.javafx.application.ParametersImpl;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lee.study.down.HttpDownProxyServer;
import lee.study.down.ca.HttpDownProxyCACertFactory;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
import lee.study.down.intercept.HttpDownHandleInterceptFactory;
import lee.study.down.model.ConfigBaseInfo;
import lee.study.down.model.ConfigInfo;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import lee.study.down.mvc.HttpDownSpringBoot;
import lee.study.down.mvc.controller.HttpDownController;
import lee.study.down.mvc.form.NewTaskForm;
import lee.study.down.plug.PluginContent;
import lee.study.down.task.HttpDownProgressEventTask;
import lee.study.down.task.PluginUpdateCheckTask;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.ConfigUtil;
import lee.study.down.util.FileUtil;
import lee.study.down.util.OsUtil;
import lee.study.down.util.PathUtil;
import lee.study.proxyee.crt.CertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class HttpDownApplication extends Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownApplication.class);

  private String url;
  private float version;
  private Stage stage;
  private Browser browser;
  private TrayIcon trayIcon;

  private static HttpDownProxyServer proxyServer;

  static {
//    System.setProperty("file.encoding","GBK");
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
      try {
        if (OsUtil.isBusyPort(tomcatPort)) {
          tomcatPort = OsUtil.getFreePort(tomcatPort + 1);
        }
      } catch (Exception e) {
        LOGGER.error("getFreePort:", e);
        JOptionPane.showMessageDialog(null, "系统异常，请尝试在命令行中执行netsh winsock reset，再运行软件", "运行警告",
            JOptionPane.ERROR_MESSAGE);
        throw e;
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
    //插件加载
    PluginContent.init();
    //程序退出监听
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        if (ContentManager.CONFIG.get().getSniffModel() != 3) {
          OsUtil.disabledProxy();
        }
      } catch (IOException e) {
        LOGGER.error("disabledProxy error", e);
      }
    }));
  }

  private void afterTrayInit() {
    //启动线程
    new HttpDownProgressEventTask().start();
    new PluginUpdateCheckTask().start();
  }

  private static boolean isSupportBrowser;

  @Override
  public void start(Stage stage) throws Exception {
    initHandle();
    this.stage = stage;
    Platform.setImplicitExit(false);
    isSupportBrowser = isSupportBrowser();
    SwingUtilities.invokeLater(this::addTray);
    //webview加载
    if (ContentManager.CONFIG.get().getUiModel() == 1 && isSupportBrowser) {
      initBrowser();
    }
    stage.setTitle("proxyee-down-" + version);
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    ConfigInfo cf = ContentManager.CONFIG.get();
    stage.setX(cf.getGuiX() >= 0 ? cf.getGuiX() : bounds.getMinX());
    stage.setY(cf.getGuiY() >= 0 ? cf.getGuiY() : bounds.getMinY());
    stage.setWidth(cf.getGuiWidth() >= 0 ? cf.getGuiWidth() : bounds.getWidth());
    stage.setHeight(cf.getGuiHeight() >= 0 ? cf.getGuiHeight() : bounds.getHeight());
    stage.getIcons().add(new Image(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("favicon.png")));
    //关闭窗口监听
    stage.setOnCloseRequest(event -> {
      event.consume();
      close();
    });
    //检查证书安装情况
    checkCa();
    //开启代理服务器
    startSniffProxy();
    //启动时是否打开窗口
    if (ContentManager.CONFIG.get().isAutoOpen()) {
      open(false);
    }
  }

  private void checkCa() {
    try {
      //根证书生成
      if (!FileUtil.exists(HttpDownConstant.CA_PRI_PATH)
          || !FileUtil.exists(HttpDownConstant.CA_CERT_PATH)) {
        //生成ca证书和私钥
        KeyPair keyPair = CertUtil.genKeyPair();
        File priKeyFile = FileUtil.createFile(HttpDownConstant.CA_PRI_PATH, true);
        File caCertFile = FileUtil.createFile(HttpDownConstant.CA_CERT_PATH, false);
        Files.write(Paths.get(priKeyFile.toURI()), keyPair.getPrivate().getEncoded());
        Files.write(Paths.get(caCertFile.toURI()),
            CertUtil.genCACert(
                "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + HttpDownConstant.CA_SUBJECT,
                new Date(),
                new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3650)),
                keyPair)
                .getEncoded());
      }
      //启动检查证书安装
      if (ContentManager.CONFIG.get().isCheckCa()
          && !OsUtil.existsCert(HttpDownConstant.CA_SUBJECT,
          ByteUtil.getCertHash(CertUtil.loadCert(HttpDownConstant.CA_CERT_PATH)))) {
        if (OsUtil.existsCert(HttpDownConstant.CA_SUBJECT)) {
          //重新生成卸载之前的证书
          if (OsUtil.isWindows() && OsUtil
              .existsWindowsCert(HttpDownConstant.CA_SUBJECT, false)) { //admin权限静默卸载
            showMsg("检测到系统存在旧的证书，请按确定再根据引导进行删除");
          }
          OsUtil.uninstallCert(HttpDownConstant.CA_SUBJECT);
        }
        if (OsUtil.isWindows() && !OsUtil.isAdmin()) { //admin权限静默安装
          showMsg("需要安装新证书，请按确定再引导进行安装");
        }
        OsUtil.installCert(HttpDownConstant.CA_CERT_PATH);
      }
    } catch (Exception e) {
      ContentManager.CONFIG.get().setCheckCa(false);
      ContentManager.CONFIG.save();
      showMsg("证书安装失败，请手动安装");
      LOGGER.error("cert handle error", e);
    }
  }

  private void startSniffProxy() {
    //嗅探代理服务器启动
    proxyServer = new HttpDownProxyServer(
        new HttpDownProxyCACertFactory(HttpDownConstant.CA_CERT_PATH, HttpDownConstant.CA_PRI_PATH),
        ContentManager.CONFIG.get().getSecProxyConfig(),
        new HttpDownHandleInterceptFactory(httpDownInfo -> Platform.runLater(() -> {
          ConfigBaseInfo configInfo = ContentManager.CONFIG.get();
          //自动开始下载
          if (configInfo.isAutoDown()) {
            NewTaskForm newTaskForm = new NewTaskForm();
            newTaskForm.setId(httpDownInfo.getTaskInfo().getId());
            newTaskForm.setFilePath(configInfo.getAutoDownPath());
            newTaskForm.setFileName(FileUtil.renameIfExists(configInfo.getAutoDownPath() + File.separator + httpDownInfo.getTaskInfo().getFileName()));
            newTaskForm.setUnzip(true);
            try {
              ResultInfo resultInfo = HttpDownController.commonStartTask(newTaskForm);
              if (resultInfo.getStatus() == ResultStatus.SUCC.getCode()) {
                trayIcon.displayMessage("提示", "新任务【" + newTaskForm.getFileName() + "】开始自动下载", TrayIcon.MessageType.INFO);
              } else {
                trayIcon.displayMessage("提示", "自动下载失败：" + resultInfo.getMsg(), MessageType.ERROR);
              }
            } catch (Exception e) {
              LOGGER.error("auto start error", e);
              trayIcon.displayMessage("提示", "自动下载失败：" + e.getMessage(), MessageType.ERROR);
            }
          } else {
            if (configInfo.getUiModel() == 1) {
              String taskId = httpDownInfo.getTaskInfo().getId();
              browser.webEngine.executeScript("vue.$children[0].openTabHandle('/tasks');"
                  + "vue.$store.commit('tasks/setNewTaskId','" + taskId + "');"
                  + "vue.$store.commit('tasks/setNewTaskStatus',2);");
            }
            open(false);
          }
        }))
    );
    int sniffProxyPort = ContentManager.CONFIG.get().getProxyPort();
    if (OsUtil.isBusyPort(sniffProxyPort)) {
      showMsg("端口(" + sniffProxyPort + ")被占用，请勿重复启动本软件！若无重复启动，请关闭占用端口的软件或设置新的端口号");
    } else {
      new Thread(() -> proxyServer.start(ContentManager.CONFIG.get().getProxyPort())).start();
    }
  }

  /**
   * 打开下载器界面
   *
   * @param isTray 是否由托盘菜单发起
   */
  public void open(boolean isTray) {
    if (browser == null || ContentManager.CONFIG.get().getUiModel() == 2) {
      try {
        OsUtil.openBrowse(url);
      } catch (Exception e) {
        LOGGER.error("openBrowse error:", e);
      }
      return;
    }
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

  public void close() {
    stage.hide();
  }

  private void showMsg(String msg) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("提示");
    alert.setHeaderText(null);
    alert.setContentText(msg);

    DialogPane root = alert.getDialogPane();
    Stage dialogStage = new Stage();

    for (ButtonType buttonType : root.getButtonTypes()) {
      ButtonBase button = (ButtonBase) root.lookupButton(buttonType);
      button.setOnAction(evt -> dialogStage.close());
    }

    root.getScene().setRoot(new Group());
    root.setPadding(new Insets(10, 0, 10, 0));

    Scene scene = new Scene(root);
    dialogStage.setScene(scene);
    dialogStage.initModality(Modality.APPLICATION_MODAL);
    dialogStage.setAlwaysOnTop(true);
    dialogStage.setResizable(false);
    dialogStage.showAndWait();
  }

  private void addTray() {
    try {
      if (SystemTray.isSupported()) {
        // 获得系统托盘对象
        SystemTray systemTray = SystemTray.getSystemTray();
        // 获取图片所在的URL
        URL url = Thread.currentThread().getContextClassLoader().getResource("favicon.png");
        trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(url), "proxyee-down");
        // 为系统托盘加托盘图标
        systemTray.add(trayIcon);
        trayIcon.setImageAutoSize(true);
        // 托盘双击事件
        trayIcon.addActionListener(event -> Platform.runLater(() -> open(true)));
        // 创建弹出菜单
        PopupMenu popupMenu = new PopupMenu();
        MenuItem tasksItem = new MenuItem("显示");
        tasksItem.addActionListener(event -> Platform.runLater(() -> open(true)));

        MenuItem crtItem = new MenuItem("证书目录");
        crtItem.addActionListener(event -> {
          try {
            Desktop.getDesktop().open(new File(HttpDownConstant.SSL_PATH));
          } catch (Exception e) {
            LOGGER.error("open cert dir error", e);
          }
        });

        Menu proxyMenu = new Menu("嗅探模式");
        if (!OsUtil.isWindows() && !OsUtil.isMac()) {
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
          try {
            //默认选中
            if (ContentManager.CONFIG.get().getSniffModel() == 1) {
              mig.selectItem(globalProxyItem);
              OsUtil.enabledHTTPProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
            } else if (ContentManager.CONFIG.get().getSniffModel() == 2) {
              mig.selectItem(bdyProxyItem);
              OsUtil.enabledPACProxy(
                  "http://127.0.0.1:" + ConfigUtil.getValue("tomcat.server.port")
                      + "/res/pd.pac?t=" + System.currentTimeMillis());
            } else {
              mig.selectItem(disableProxyItem);
            }
          } catch (Exception e) {
            LOGGER.error("set proxy error", e);
          }
          mig.addActionListener(event -> {
            try {
              String selectedItemName = ((CheckboxMenuItem) event.getSource()).getName();
              if ("1".equals(selectedItemName)) {
                ContentManager.CONFIG.get().setSniffModel(1);
                OsUtil.enabledHTTPProxy("127.0.0.1", ContentManager.CONFIG.get().getProxyPort());
              } else if ("2".equals(selectedItemName)) {
                ContentManager.CONFIG.get().setSniffModel(2);
                OsUtil.enabledPACProxy(
                    "http://127.0.0.1:" + ConfigUtil.getValue("tomcat.server.port")
                        + "/res/pd.pac?t=" + System.currentTimeMillis());
              } else {
                ContentManager.CONFIG.get().setSniffModel(3);
                OsUtil.disabledProxy();
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
        if (isSupportBrowser) {
          uiMenu.add(guiItem);
        } else {
          ContentManager.CONFIG.get().setUiModel(2);
          ContentManager.CONFIG.save();
        }
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
              ContentManager.CONFIG.get().setUiModel(1);
              initBrowser();
            } else {
              ContentManager.CONFIG.get().setUiModel(2);
              destroyBrowser();
              stage.close();
            }
            open(true);
            ContentManager.CONFIG.save();
          });
        });

        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.addActionListener(event -> Platform.runLater(() -> {
          if (ContentManager.CONFIG.get().getUiModel() == 1) {
            browser.webEngine.executeScript("vue.$children[0].openTabHandle('/about');");
          }
          open(true);
        }));

        MenuItem closeItem = new MenuItem("退出");
        closeItem.addActionListener(event -> {
          //记录窗口信息
          ContentManager.CONFIG.get().setGuiX(stage.getX());
          ContentManager.CONFIG.get().setGuiY(stage.getY());
          ContentManager.CONFIG.get().setGuiHeight(stage.getHeight());
          ContentManager.CONFIG.get().setGuiWidth(stage.getWidth());
          ContentManager.CONFIG.save();
          System.exit(0);
        });

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
    afterTrayInit();
  }

  private void initBrowser() {
    if (this.browser == null) {
      this.browser = new Browser();
      stage.setScene(new Scene(browser));
    }
    browser.load(this.url);
  }

  private boolean isSupportBrowser() {
    try {
      new WebView();
      return true;
    } catch (Throwable e) {
      return false;
    }
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

    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();

    public Browser() {
      getChildren().add(webView);
      webView.setContextMenuEnabled(false);
      //自定义webview右键菜单
      final Clipboard clipboard = Clipboard.getSystemClipboard();
      ContextMenu contextMenu = new ContextMenu();
      javafx.scene.control.MenuItem copy = new javafx.scene.control.MenuItem("复制");
      copy.setOnAction(e -> {
        ClipboardContent content = new ClipboardContent();
        Object selection = webView.getEngine().executeScript("window.getSelection().toString()");
        if (selection != null) {
          content.putString(selection.toString());
          clipboard.setContent(content);
        }
      });
      javafx.scene.control.MenuItem paste = new javafx.scene.control.MenuItem("粘帖");
      paste.setOnAction(e -> {
        Object content = clipboard.getContent(DataFormat.PLAIN_TEXT);
        if (content != null) {
          webView.getEngine().executeScript("if(document.activeElement.nodeName.toUpperCase()=='INPUT'){"
              + "document.activeElement.value='" + content + "';"
              + "var event = document.createEvent('Event');"
              + "event.initEvent('input', true, true);"
              + "document.activeElement.dispatchEvent(event);}");
        }
      });
      contextMenu.getItems().addAll(copy, paste);
      webView.setOnMousePressed(e -> {
        if (e.getButton() == MouseButton.SECONDARY) {
          contextMenu.show(webView, e.getScreenX(), e.getScreenY());
        } else {
          contextMenu.hide();
        }
      });
    }

    @Override
    protected void layoutChildren() {
      double w = getWidth();
      double h = getHeight();
      layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    public void load(String url) {
      webEngine.load(url);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}