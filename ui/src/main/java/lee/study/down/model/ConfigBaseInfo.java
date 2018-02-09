package lee.study.down.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ConfigBaseInfo implements Serializable {

  private int proxyPort;  //代理端口号
  private int sniffModel;  //嗅探模式 1.全网 2.百度云 3.关闭
  private int uiModel;  //嗅探模式 1.GUI 2.浏览器
  private int connections;  //默认分段数
  private int timeout;  //超时重试时间
  private boolean secProxyEnable; //二级代理开关
  private String lastPath;  //最后保存文件的路径
  private int retryCount;  //失败重试次数
}
