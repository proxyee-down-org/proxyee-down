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
 * 30秒内没有下载判断为失败，进行重试
 */
public class HttpDownErrorCheckTask extends Thread {

  @Override
  public void run() {
    Map<String, Long> flagMap = new HashMap<>();
    while (true) {
      try {
        if (HttpDownServer.DOWN_CONTENT != null && HttpDownServer.DOWN_CONTENT.size() > 0) {
          for (Entry<String, HttpDownInfo> entry : HttpDownServer.DOWN_CONTENT.entrySet()) {
            TaskInfo taskInfo = entry.getValue().getTaskInfo();
            if (taskInfo.getChunkInfoList() != null) {
              for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                //下载中或者下载失败的情况下30秒没有反应则重新建立连接下载
                if (taskInfo.getStatus() == 1 && (chunkInfo.getStatus() == 1
                    || chunkInfo.getStatus() == 3)) {
                  String key = taskInfo.getId() + "_" + chunkInfo.getIndex();
                  Long downSize = flagMap.get(key);
                  //下载失败
                  if (downSize != null && downSize == chunkInfo.getDownSize()) {
                    HttpDownServer.LOGGER.debug(
                        "30秒内无响应重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                    //避免同时下载
                    HttpDownUtil.retryDown(taskInfo, chunkInfo);
                  } else {
                    flagMap.put(key, chunkInfo.getDownSize());
                  }
                }
                  /*
                  4为暂停，需手动开始下载
                  else if (chunkInfo.getStatus() == 4) {
                    HttpDownServer.LOGGER.debug(
                        "启动下载重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                    HttpDownUtil.retryDown(taskInfo, chunkInfo);
                  }*/
              }
            }

          }
        }
        TimeUnit.MILLISECONDS.sleep(30000);
      } catch (Exception e) {
        HttpDownServer.LOGGER.error("checkTask:" + e);
      }
    }
  }
}
