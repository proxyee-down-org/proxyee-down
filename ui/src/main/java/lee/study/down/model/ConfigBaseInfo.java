package lee.study.down.model;

import lombok.Data;

@Data
public class ConfigBaseInfo {

  private int proxyPort = 9999;  //代理端口号
  private int sniffModel = 2;  //嗅探模式 1.全网 2.百度云 3.关闭
  private int uiModel = 1;  //嗅探模式 1.GUI 2.浏览器
  private int connections = 32;  //默认分段数
  private int timeout = 10;  //超时重试时间
  private boolean secProxyEnable; //二级代理开关
  private String lastPath;  //最后保存文件的路径
  private int retryCount = 5;  //失败重试次数
  private boolean checkCa = true; //启动是否自动检测和安装ca证书
  private double guiWidth = -1;
  private double guiHeight = -1;
  private double guiX = -1;
  private double guiY = -1;
  private boolean autoOpen = true; //启动成功时窗口是否显示
  private boolean autoDown = false; //嗅探到任务时是否自动开始下载
  private String autoDownPath;  //自动下载保存文件的路径
}
