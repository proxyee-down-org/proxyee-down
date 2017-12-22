package lee.study.down.dispatch;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.WsUtil;

public class HttpDownProgressEventTask extends Thread {

  private int secCount = 0;

  @Override
  public void run() {

    while (true) {
      try {
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
              if (++secCount == 2) {  //一秒保存一次进度
                secCount = 0;
                //保存任务进度记录
                synchronized (taskInfo) {
                  if (taskInfo.getStatus() == 1) {
                    ByteUtil.serialize(HttpDownServer.DOWN_CONTENT.get(taskInfo.getId()),
                        taskInfo.buildTaskFilePath() + ".inf");
                  }
                }
              }
              WsUtil.sendMsg();
            }
          }
        }
        TimeUnit.MILLISECONDS.sleep(500);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
