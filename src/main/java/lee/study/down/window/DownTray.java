package lee.study.down.window;

import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;

public class DownTray {

  public static void main(String[] args) {
      start(9000);
  }

  public static void start(int port) {
    // 判断是否支持系统托盘
      // 获取图片所在的URL
      URL url = Thread.currentThread().getContextClassLoader().getResource("favicon.png");
      TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(url), "proxyee-down");
      trayIcon.setImageAutoSize(true);
      // 为托盘添加鼠标适配器
      trayIcon.addMouseListener(new MouseAdapter() {
        // 鼠标事件
        public void mouseClicked(MouseEvent e) {
          // 判断是否双击了鼠标
          if (e.getClickCount() == 2) {
            try {
              Desktop desktop = Desktop.getDesktop();
              if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = new URI("http://127.0.0.1:" + port);
                desktop.browse(uri);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      });
      // 创建弹出菜单
      PopupMenu popupMenu = new PopupMenu();
      String a = "测试";
      popupMenu.add(new MenuItem(a));
      popupMenu.addSeparator();
      popupMenu.add(new MenuItem(a));

      // 为托盘图标加弹出菜弹
      trayIcon.setPopupMenu(popupMenu);
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      try {
        // 为系统托盘加托盘图标
        systemTray.add(trayIcon);
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

}
