package lee.study.down.task;

import java.util.concurrent.TimeUnit;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.HttpDownContent;
import lee.study.down.content.HttpWsContent;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownProgressEventTask extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownProgressEventTask.class);

  @Override
  public void run() {

    while (true) {
      try {
        for (TaskInfo taskInfo : HttpDownContent.getStartTasks()) {
          if (taskInfo.getStatus() != HttpDownStatus.DONE
              && taskInfo.getStatus() != HttpDownStatus.PAUSE) {
            taskInfo.setLastTime(System.currentTimeMillis());
            for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
              if (chunkInfo.getStatus() != HttpDownStatus.DONE
                  && chunkInfo.getStatus() != HttpDownStatus.PAUSE) {
                chunkInfo.setLastTime(System.currentTimeMillis());
              }
            }
            //保存任务进度记录
            synchronized (taskInfo) {
              if (taskInfo.getStatus() != HttpDownStatus.DONE) {
                HttpDownContent.saveTask(taskInfo.getId());
              }
            }
          }
        }
        HttpWsContent.sendMsg();
        TimeUnit.MILLISECONDS.sleep(1000);
      } catch (Exception e) {
        LOGGER.error("eventTask:", e);
      }
    }
  }
}
