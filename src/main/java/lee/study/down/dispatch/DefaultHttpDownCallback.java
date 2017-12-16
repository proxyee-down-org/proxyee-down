package lee.study.down.dispatch;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskBaseInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.HttpDownUtil;

public class DefaultHttpDownCallback implements HttpDownCallback {

  @Override
  public void start(TaskInfo taskInfo) {
    try {
      //保存下载记录
      HttpDownServer.RECORD_CONTENT.put(taskInfo.getId(), taskInfo);
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      //保存任务进度记录
      ByteUtil.serialize(HttpDownServer.DOWN_CONTENT.get(taskInfo.getId()),
          taskInfo.getFilePath() + File.separator + taskInfo.getFileName() + ".inf");
    } catch (IOException e) {
      e.printStackTrace();
    }
    //标记为下载中并记录开始时间
    HttpDownServer.sendMsg("start", taskInfo);
  }

  @Override
  public void chunkStart(TaskInfo taskInfo, ChunkInfo chunkInfo) {

  }

  @Override
  public void progress(TaskInfo taskInfo, ChunkInfo chunkInfo) {

  }

  @Override
  public void error(TaskInfo taskInfo, ChunkInfo chunkInfo, Throwable cause) {
    try {
      HttpDownUtil.retryDown(taskInfo, chunkInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void chunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo) {
    HttpDownServer.sendMsg("chunkDone", taskInfo);
  }

  @Override
  public void done(TaskInfo taskInfo) {
    try {
      //更改任务下载状态为已完成
      if (taskInfo.getTotalSize() == -1) {  //chunked编码最后更新文件大小
        taskInfo.setTotalSize(taskInfo.getDownSize());
        taskInfo.getChunkInfoList().get(0).setTotalSize(taskInfo.getDownSize());
      }
      TaskBaseInfo taskBaseInfo = HttpDownServer.RECORD_CONTENT.get(taskInfo.getId());
      taskBaseInfo.setStatus(2);
      if (taskBaseInfo.getTotalSize() == -1) {  //chunked编码最后更新文件大小
        taskBaseInfo.setTotalSize(taskInfo.getDownSize());
      }
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      //删除任务进度记录文件
      synchronized (taskInfo) {
        Files.deleteIfExists(
            Paths.get(taskInfo.getFilePath() + File.separator + taskInfo.getFileName() + ".inf"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    HttpDownServer.sendMsg("done", taskInfo);
  }

  private static void updateTotalSize(TaskBaseInfo taskInfo) {

  }
}
