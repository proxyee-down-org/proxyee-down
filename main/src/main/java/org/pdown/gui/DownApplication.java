package org.pdown.gui;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pdown.gui.com.Browser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.pdown.rest")
public class DownApplication extends AbstractJavaFxApplicationSupport {

  private static final String ICON_NAME = "icon.png";

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
      trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(url), "proxyee-down");
      // 为系统托盘加托盘图标
      systemTray.add(trayIcon);
      trayIcon.setImageAutoSize(true);

      //添加右键菜单
      PopupMenu popupMenu = new PopupMenu();
      MenuItem showItem = new MenuItem("显示");
      showItem.addActionListener(event -> Platform.runLater(() -> show()));
      MenuItem closeItem = new MenuItem("退出");
      closeItem.addActionListener(event -> System.exit(0));
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
    stage.getIcons().add(new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(ICON_NAME)));
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


  public static void main(String[] args) {
    launch(DownApplication.class, null, args);
  }
}
