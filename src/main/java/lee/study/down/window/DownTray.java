package lee.study.down.window;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import javax.swing.JFrame;
import lee.study.down.HttpDownServer;
import lee.study.down.util.OsUtil;
import lee.study.proxyee.server.HttpProxyServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class DownTray extends JFrame{

  public DownTray(){
    super();
    init();
  }

  private void init() {
    // 判断是否支持系统托盘
    if (SystemTray.isSupported()) {
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
            openBrowse();
          }
        }
      });
      // 创建弹出菜单
      PopupMenu popupMenu = new PopupMenu();
      MenuItem openItem = new MenuItem("打开");
      MenuItem closeItem = new MenuItem("关闭");
      popupMenu.add(openItem);
//      popupMenu.addSeparator();
      popupMenu.add(closeItem);
      openItem.addActionListener((event)-> openBrowse());
      closeItem.addActionListener((event)-> {
        System.exit(0);
      });
      // 为托盘图标加弹出菜弹
      trayIcon.setPopupMenu(popupMenu);
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      try {
        // 为系统托盘加托盘图标
        systemTray.add(trayIcon);
      } catch (Exception e) {
        HttpDownServer.LOGGER.warn("init",e);
      }
    }
    openBrowse();
  }

  private void openBrowse(){
    try {
      Desktop desktop = Desktop.getDesktop();
      if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
        URI uri = new URI("http://127.0.0.1:" + HttpDownServer.VIEW_SERVER_PORT);
        desktop.browse(uri);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
