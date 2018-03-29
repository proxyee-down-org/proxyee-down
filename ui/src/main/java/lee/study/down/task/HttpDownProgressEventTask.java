package lee.study.down.task;

import java.util.concurrent.TimeUnit;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.ContentManager;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.mvc.controller.HttpDownController;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownProgressEventTask extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownProgressEventTask.class);

  @Override
  public void run() {

    while (true) {
      try {
        for (TaskInfo taskInfo : ContentManager.DOWN.getStartTasks()) {
          if (taskInfo.getStatus() != HttpDownStatus.DONE
              && taskInfo.getStatus() != HttpDownStatus.FAIL
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
                ContentManager.DOWN.saveTask(taskInfo.getId());
              }
            }
          }
        }
        ContentManager.WS.sendMsg(ContentManager.DOWN.buildDowningWsForm());
        if (HttpDownController.updateBootstrap != null
            && HttpDownController.updateBootstrap.getHttpDownInfo().getTaskInfo().getStatus()
            != HttpDownStatus.DONE) {
          ContentManager.WS.sendMsg(new WsForm(WsDataType.UPDATE_PROGRESS,
              HttpDownController.updateBootstrap.getHttpDownInfo().getTaskInfo()));
        }
        TimeUnit.MILLISECONDS.sleep(1000);
      } catch (Exception e) {
        LOGGER.error("eventTask:", e);
      }
    }
  }
}
