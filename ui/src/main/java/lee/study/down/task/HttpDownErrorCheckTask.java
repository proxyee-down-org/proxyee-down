package lee.study.down.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.ContentManager;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 30秒内没有下载判断为失败，进行重试
 */
public class HttpDownErrorCheckTask extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownErrorCheckTask.class);

  @Override
  public void run() {
    Map<String, Long> flagMap = new HashMap<>();
    while (true) {
      try {
        for (TaskInfo taskInfo : ContentManager.DOWN.getStartTasks()) {
          if (taskInfo.getChunkInfoList() != null) {
            for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
              //30秒没有反应则重新建立连接下载
              if (taskInfo.getStatus() == HttpDownStatus.RUNNING
                  && chunkInfo.getStatus() != HttpDownStatus.DONE
                  && chunkInfo.getStatus() != HttpDownStatus.PAUSE) {
                String key = taskInfo.getId() + "_" + chunkInfo.getIndex();
                Long downSize = flagMap.get(key);
                //下载失败
                if (downSize != null && downSize == chunkInfo.getDownSize()) {
                  LOGGER.debug(
                      "30秒内无响应重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                  //避免同时下载
                  ContentManager.DOWN.getBoot(taskInfo.getId()).retryChunkDown(chunkInfo);
                } else {
                  flagMap.put(key, chunkInfo.getDownSize());
                }
              }
            }
          }

        }
        TimeUnit.SECONDS.sleep(ContentManager.CONFIG.get().getTimeout());
      } catch (Exception e) {
        LOGGER.error("checkTask:" + e);
      }
    }
  }
}
