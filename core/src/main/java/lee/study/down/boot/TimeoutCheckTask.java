package lee.study.down.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutCheckTask extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutCheckTask.class);

  private static int seconds = 30;
  private static Map<String, AbstractHttpDownBootstrap> bootstrapContent = new ConcurrentHashMap<>();

  public synchronized static void setTimeout(int seconds) {
    TimeoutCheckTask.seconds = seconds;
  }

  public void addBoot(AbstractHttpDownBootstrap bootstrap) {
    bootstrapContent.put(bootstrap.getHttpDownInfo().getTaskInfo().getId(), bootstrap);
  }

  public void delBoot(String id) {
    bootstrapContent.remove(id);
  }

  @Override
  public void run() {
    Map<String, Long> flagMap = new HashMap<>();
    while (true) {
      try {
        for (AbstractHttpDownBootstrap bootstrap : bootstrapContent.values()) {
          TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
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
                  LOGGER.debug(seconds + "秒内无响应重试：" + chunkInfo);
                  //避免同时下载
                  bootstrap.retryChunkDown(chunkInfo);
                } else {
                  flagMap.put(key, chunkInfo.getDownSize());
                }
              }
            }
          }

        }
        TimeUnit.SECONDS.sleep(TimeoutCheckTask.seconds);
      } catch (Exception e) {
        LOGGER.error("checkTask:" + e);
      }
    }
  }
}
