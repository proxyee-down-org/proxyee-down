package lee.study.down.gui;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.JFrame;
import lee.study.down.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统托盘
 */
public class HttpDownTray extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownTray.class);

  private static final long serialVersionUID = 7380717165373045846L;

  private String homeUrl;
  private TrayIcon trayIcon;

  public HttpDownTray(String homeUrl) {
    super();
    this.homeUrl = homeUrl;
  }

  public void init() throws Exception {
    // 判断是否支持系统托盘
    if (SystemTray.isSupported()) {
      // 获取图片所在的URL
      URL url = Thread.currentThread().getContextClassLoader().getResource("favicon.png");
      trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(url), "proxyee-down");
      trayIcon.setImageAutoSize(true);
      // 为托盘添加鼠标适配器
      trayIcon.addMouseListener(new MouseAdapter() {
        // 鼠标事件
        public void mouseClicked(MouseEvent event) {
          // 判断是否双击了鼠标
          if (event.getClickCount() == 2) {
            try {
              OsUtil.openBrowse(homeUrl);
            } catch (Exception e) {
              LOGGER.error("openBrowse:", e);
            }
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
      openItem.addActionListener((event) -> {
        try {
          OsUtil.openBrowse(homeUrl);
        } catch (Exception e) {
          LOGGER.error("openBrowse:", e);
        }
      });
      closeItem.addActionListener((event) -> System.exit(0));
      // 为托盘图标加弹出菜弹
      trayIcon.setPopupMenu(popupMenu);
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      // 为系统托盘加托盘图标
      systemTray.add(trayIcon);
    }
    showMsg("软件启动成功！");
  }


  public void showMsg(String msg) {
    trayIcon.displayMessage("提示", msg, TrayIcon.MessageType.INFO);
  }


}
