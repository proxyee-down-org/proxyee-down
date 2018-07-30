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
import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.com.Browser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.pdown.rest")
public class DownApplication extends AbstractJavaFxApplicationSupport {

  private static final String OS = OsUtil.isWindows() ? "windows"
      : (OsUtil.isMac() ? "mac" : "linux");
  private static final String ICON_NAME = OS + "/logo.png";

  private Stage stage;
  private Browser browser;
  private TrayIcon trayIcon;

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.stage = primaryStage;
    Platform.setImplicitExit(false);
    loadTray();
    loadBrowser();
    loadWindow();
    show();
  }

  //加载托盘
  private void loadTray() throws AWTException {
    if (SystemTray.isSupported()) {
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      // 获取图片所在的URL
      URL url = Thread.currentThread().getContextClassLoader().getResource(ICON_NAME);
      Image trayImage = Toolkit.getDefaultToolkit().getImage(url);
      Dimension trayIconSize = systemTray.getTrayIconSize();
      trayImage = trayImage.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
      trayIcon = new TrayIcon(trayImage, "proxyee-down");
      // 为系统托盘加托盘图标
      systemTray.add(trayIcon);

      //添加右键菜单
      PopupMenu popupMenu = new PopupMenu();
      MenuItem showItem = new MenuItem("显示");
      showItem.addActionListener(event -> Platform.runLater(() -> show()));
      MenuItem closeItem = new MenuItem("退出");
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
  private void loadBrowser() throws AWTException {
    this.browser = new Browser();
    stage.setScene(new Scene(browser));
    browser.load("http://www.baidu.com");
  }

  //加载gui窗口
  private void loadWindow() {
    stage.setTitle("proxyee-down");
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());
    stage.getIcons().add(new javafx.scene.image.Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(ICON_NAME)));
    //关闭窗口监听
    stage.setOnCloseRequest(event -> event.consume());
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

  private void alert(String msg) {
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


  public static void main(String[] args) {
    launch(DownApplication.class, null, args);
  }
}
