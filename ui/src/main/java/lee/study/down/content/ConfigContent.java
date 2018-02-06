package lee.study.down.content;

import java.io.IOException;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.model.ConfigInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lee.study.down.util.WindowsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigContent {

  private final static Logger LOGGER = LoggerFactory.getLogger(ConfigContent.class);

  //配置对象管理
  private static ConfigInfo configContent;

  public void set(ConfigInfo configInfo) {
    configContent = configInfo;
  }

  public ConfigInfo get() {
    return configContent;
  }

  public void save() {
    synchronized (configContent) {
      try {
        ByteUtil.serialize(configContent, HttpDownConstant.CONFIG_PATH);
      } catch (IOException e) {
        LOGGER.error("写入配置文件失败：", e);
      }
    }
  }

  public void init() {
    if (FileUtil.exists(HttpDownConstant.CONFIG_PATH)) {
      try {
        configContent = (ConfigInfo) ByteUtil.deserialize(HttpDownConstant.CONFIG_PATH);
      } catch (Exception e) {
        LOGGER.error("加载配置文件失败：", e);
      }
    }
    if (configContent == null) {
      configContent = new ConfigInfo();
      //默认代理端口
      configContent.setProxyPort(9999);
      //默认分段数
      configContent.setConnections(16);
      //默认30秒无响应重试
      configContent.setTimeout(30);
      //默认百度云嗅探模式
      configContent.setSniffModel(2);
      //默认重试次数
      configContent.setRetryCount(5);
      //安装证书
      try {
        WindowsUtil.installCert(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("ca.crt"));
      } catch (IOException e) {
        LOGGER.error("install cert error：", e);
      }
      save();
    }
  }
}
