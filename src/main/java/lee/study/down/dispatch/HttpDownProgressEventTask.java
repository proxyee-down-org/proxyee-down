package lee.study.down.dispatch;

import java.io.File;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;

public class HttpDownProgressEventTask extends Thread {

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
            //保存任务进度记录
            ByteUtil.serialize(HttpDownServer.DOWN_CONTENT.get(taskInfo.getId()),
                taskInfo.getFilePath() + File.separator + taskInfo.getFileName() + ".inf");
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
