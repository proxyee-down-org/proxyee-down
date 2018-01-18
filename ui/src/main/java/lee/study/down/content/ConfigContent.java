package lee.study.down.content;

import java.io.IOException;
import java.io.Serializable;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.model.ConfigInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

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
      save();
    }
  }
}
