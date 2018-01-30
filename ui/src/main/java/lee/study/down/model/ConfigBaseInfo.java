package lee.study.down.model;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ConfigBaseInfo implements Serializable {

  private boolean guideFlag;  //是否需要新手引导教程
  private int proxyPort;  //代理端口号
  private int proxyModel;  //全局代理 0.关闭 1.开启
  private int connections;  //默认分段数
  private int timeout;  //超时重试时间
  private boolean secProxyEnable; //二级代理开关
  private String lastPath;  //最后保存文件的路径
  private int retryCount;  //失败重试次数
}
