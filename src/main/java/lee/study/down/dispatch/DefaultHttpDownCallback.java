package lee.study.down.dispatch;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;
import lee.study.down.util.WsUtil;

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
      HttpDownServer.LOGGER.error("call start:"+e);
    }
    //标记为下载中并记录开始时间
    WsUtil.sendMsg();
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
      HttpDownServer.LOGGER.error("call error:"+e);
    }
  }

  @Override
  public void chunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo) {
    WsUtil.sendMsg();
  }

  @Override
  public void merge(TaskInfo taskInfo) {
    try {
      //更改任务下载状态为合并中
      ByteUtil.serialize(HttpDownServer.DOWN_CONTENT.get(taskInfo.getId()),
          taskInfo.buildTaskFilePath() + ".inf");
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      WsUtil.sendMsg();
    } catch (IOException e) {
      HttpDownServer.LOGGER.error("call merge:"+e);
    }
  }

  @Override
  public void done(TaskInfo taskInfo) {
    try {
      //更改任务下载状态为已完成
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      //删除临时文件
      FileUtil.deleteIfExists(taskInfo.buildChunksPath());
      //删除任务进度记录文件
      synchronized (taskInfo) {
        Files.deleteIfExists(Paths.get(taskInfo.buildTaskFilePath() + ".inf"));
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("call done:"+e);
    }
    WsUtil.sendMsg();
  }
}
