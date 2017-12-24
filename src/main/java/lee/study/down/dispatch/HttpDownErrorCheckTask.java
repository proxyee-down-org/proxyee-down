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
            if (taskInfo.getStatus() == 5) {  //合并
              HttpDownUtil.startMerge(taskInfo);
              //文件下载完成回调
              taskInfo.setStatus(2);
              taskInfo.getCallback().done(taskInfo);
            } else {
              if (taskInfo.getChunkInfoList() != null) {
                for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                  if (taskInfo.getStatus() == 1 && chunkInfo.getStatus() == 1) {
                    String key = taskInfo.getId() + "_" + chunkInfo.getIndex();
                    Long downSize = flagMap.get(key);
                    //下载失败
                    if (downSize != null && downSize == chunkInfo.getDownSize()) {
                      synchronized (chunkInfo){
                        if(chunkInfo.getStatus()==1){
                          System.out.println(
                              "30秒内无响应重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                          chunkInfo.setStatus(3);
                          HttpDownUtil.retryDown(taskInfo, chunkInfo);
                        }else{
                          HttpDownUtil.safeClose(chunkInfo.getChannel(),chunkInfo.getFileChannel());
                        }
                      }

                    } else {
                      flagMap.put(key, chunkInfo.getDownSize());
                    }
                  } else if (chunkInfo.getStatus() == 4) {
                    System.out.println(
                        "启动下载重试：" + chunkInfo.getIndex() + "\t" + chunkInfo.getDownSize());
                    HttpDownUtil.retryDown(taskInfo, chunkInfo);
                  }
                }
              }
            }

          }
        }
        TimeUnit.MILLISECONDS.sleep(30000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
