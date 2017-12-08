package lee.study.down.dispatch;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;

public class HttpDownProgressPushTask extends Thread{

  @Override
  public void run() {
    try {
      while (true) {
        if (HttpDownServer.DOWN_CONTENT != null && HttpDownServer.DOWN_CONTENT.size() > 0) {
          for (Entry<String, HttpDownInfo> entry : HttpDownServer.DOWN_CONTENT.entrySet()) {
            TaskInfo taskInfo = entry.getValue().getTaskInfo();
            if (taskInfo.getStatus() == 1) {
              taskInfo.setLastTime(System.currentTimeMillis());
              for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                if (chunkInfo.getStatus() == 1) {
                  chunkInfo.setLastTime(System.currentTimeMillis());
                }
              }
            }
            HttpDownServer.sendMsg("progress", taskInfo);
          }
          TimeUnit.MILLISECONDS.sleep(200);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
