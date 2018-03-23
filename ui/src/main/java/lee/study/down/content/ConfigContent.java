package lee.study.down.content;

import com.alibaba.fastjson.JSON;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lee.study.down.boot.TimeoutCheckTask;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.model.ConfigInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigContent {

  private final static Logger LOGGER = LoggerFactory.getLogger(ConfigContent.class);

  //配置对象管理
  private static ConfigInfo configContent;

  public void set(ConfigInfo configInfo) {
    configContent = configInfo;
    //设置超时检测时间
    TimeoutCheckTask.setTimeout(configInfo.getTimeout());
  }

  public ConfigInfo get() {
    return configContent;
  }

  public void save() {
    synchronized (configContent) {
      try {
        JSON.writeJSONString(new FileOutputStream(HttpDownConstant.CONFIG_PATH), configContent);
      } catch (IOException e) {
        LOGGER.error("写入配置文件失败：", e);
      }
    }
  }

  public void init() {
    if (FileUtil.exists(HttpDownConstant.CONFIG_PATH)) {
      try {
        set(JSON.parseObject(new FileInputStream(HttpDownConstant.CONFIG_PATH), ConfigInfo.class));
      } catch (Exception e) {
        LOGGER.error("加载配置文件失败：", e);
      }
    }
    if (configContent == null || configContent.getProxyPort() == 0) {
      set(new ConfigInfo());
      save();
    }
  }
}
