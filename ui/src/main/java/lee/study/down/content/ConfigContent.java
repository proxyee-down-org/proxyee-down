package lee.study.down.content;

import com.alibaba.fastjson.JSON;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lee.study.down.boot.TimeoutCheckTask;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.model.ConfigInfo;
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
      try (
          OutputStream outputStream = new FileOutputStream(HttpDownConstant.CONFIG_PATH)
      ) {
        JSON.writeJSONString(outputStream, configContent);
      } catch (IOException e) {
        LOGGER.error("写入配置文件失败：", e);
      }
    }
  }

  public void init() {
    if (FileUtil.exists(HttpDownConstant.CONFIG_PATH)) {
      try (
          InputStream inputStream = new FileInputStream(HttpDownConstant.CONFIG_PATH)
      ) {
        set(JSON.parseObject(inputStream, ConfigInfo.class));
      } catch (Exception e) {
        if (!(e instanceof FileNotFoundException)) {
          try {
            FileUtil.deleteIfExists(HttpDownConstant.CONFIG_PATH);
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }
        LOGGER.error("加载配置文件失败：", e);
      }
    }
    if (configContent == null || configContent.getProxyPort() == 0) {
      set(new ConfigInfo());
      save();
    }
  }
}
