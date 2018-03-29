package lee.study.down.boot;

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

  private static volatile int seconds = 30;
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
    while (true) {
      try {
        for (AbstractHttpDownBootstrap bootstrap : bootstrapContent.values()) {
          TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
          if (taskInfo.getChunkInfoList() != null) {
            for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
              //30秒没有反应则重新建立连接下载
              if (taskInfo.getStatus() == HttpDownStatus.RUNNING
                  && chunkInfo.getStatus() != HttpDownStatus.DONE
                  && chunkInfo.getStatus() != HttpDownStatus.WAIT
                  && chunkInfo.getStatus() != HttpDownStatus.PAUSE) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - chunkInfo.getLastDownTime() > seconds * 1000) {
                  LOGGER.debug(seconds + "秒内无响应重试：" + chunkInfo);
                  if (chunkInfo.getStatus() == HttpDownStatus.ERROR_WAIT_CONNECT) {
                    chunkInfo.setErrorCount(chunkInfo.getErrorCount() + 1);
                  }
                  //重试下载
                  bootstrap.retryChunkDown(chunkInfo);
                }
              }
            }
          }

        }
        TimeUnit.MILLISECONDS.sleep(1000);
      } catch (Exception e) {
        LOGGER.error("checkTask:" + e);
      }
    }
  }
}
