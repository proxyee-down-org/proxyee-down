package lee.study.down.gui;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeoutException;
import javax.swing.JFrame;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
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
              LOGGER.error("open tasks page:", e);
            }
          }
        }
      });
      // 创建弹出菜单
      PopupMenu popupMenu = new PopupMenu();
      MenuItem tasksItem = new MenuItem("任务列表");
      tasksItem.addActionListener((event) -> {
        try {
          OsUtil.openBrowse(homeUrl);
        } catch (Exception e) {
          LOGGER.error("openBrowse error:", e);
          showMsg("打开任务列表失败：" + e.toString());
        }
      });

      MenuItem crtItem = new MenuItem("安装证书");
      crtItem.addActionListener((event) -> {
        try {
          URL crtUrl = new URL(
              "http://127.0.0.1:" + ContentManager.CONFIG.get().getProxyPort() + "/ca.crt");
          URLConnection connection = crtUrl.openConnection();
          OsUtil.execFile(connection.getInputStream(),
              HttpDownConstant.HOME_PATH + File.separator + "ca.crt");
        } catch (Exception e) {
          LOGGER.error("install crt error:", e);
          showMsg("证书安装失败：" + e.toString());
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
        mig.addActionListener((event) -> {
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
            showMsg("全局代理设置失败：" + e.toString());
          }
        });
      }

      MenuItem aboutItem = new MenuItem("关于");
      aboutItem.addActionListener((event) -> {
        try {
          OsUtil.openBrowse(homeUrl + "/#/about");
        } catch (Exception e) {
          LOGGER.error("open about page error:", e);
          showMsg("打开任务列表失败：" + e.toString());
        }
      });

      MenuItem closeItem = new MenuItem("退出");
      closeItem.addActionListener((event) -> {
        try {
          OsUtil.disabledIEProxy();
        } catch (IOException e) {
        }
        System.exit(0);
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
      // 获得系统托盘对象
      SystemTray systemTray = SystemTray.getSystemTray();
      // 为系统托盘加托盘图标
      systemTray.add(trayIcon);
    }
    showMsg("软件启动成功！");
  }


  public void showMsg(String msg) {
    if (trayIcon != null) {
      trayIcon.displayMessage("提示", msg, TrayIcon.MessageType.INFO);
    } else {
      System.out.println(msg + " 主页请访问" + homeUrl);
    }
  }

}
