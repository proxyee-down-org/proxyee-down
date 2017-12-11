package lee.study.down.dispatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.HttpDownUtil;

/**
 * 1分钟内没有下载判断为失败，进行重试
 */
public class HttpDownErrorCheckTask extends Thread {

  @Override
  public void run() {
    try {
      Map<String, Long> flagMap = new HashMap<>();
      while (true) {
        if (HttpDownServer.DOWN_CONTENT != null && HttpDownServer.DOWN_CONTENT.size() > 0) {
          for (Entry<String, HttpDownInfo> entry : HttpDownServer.DOWN_CONTENT.entrySet()) {
            TaskInfo taskInfo = entry.getValue().getTaskInfo();
            if (taskInfo.getStatus() == 1) {
              for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                //待下载
                if (chunkInfo.getStatus() == 0) {
                  System.out.println(
                      "启动下载重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                  HttpDownUtil.retryDown(taskInfo, chunkInfo);
                } else if (chunkInfo.getStatus() == 1) {
                  String key = taskInfo.getId() + "_" + chunkInfo.getIndex();
                  Long downSize = flagMap.get(key);
                  //下载失败
                  if ((downSize != null && downSize == chunkInfo.getDownSize())) {
                    System.out.println(
                        "60秒内无响应重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                    chunkInfo.setStatus(3);
                    HttpDownUtil.retryDown(taskInfo, chunkInfo);
                  } else {
                    flagMap.put(key, chunkInfo.getDownSize());
                  }
                }
              }
            }
          }
          TimeUnit.MILLISECONDS.sleep(60000);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
