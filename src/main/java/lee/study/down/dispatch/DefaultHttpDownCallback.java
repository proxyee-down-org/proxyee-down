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
  public void onStart(TaskInfo taskInfo) {
    try {
      //保存下载记录
      HttpDownServer.RECORD_CONTENT.put(taskInfo.getId(), taskInfo);
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      //保存任务进度记录
      ByteUtil.serialize(HttpDownServer.DOWN_CONTENT.get(taskInfo.getId()),
          taskInfo.getFilePath() + File.separator + taskInfo.getFileName() + ".inf");
    } catch (IOException e) {
      HttpDownServer.LOGGER.error("call onStart:" + e);
    }
    //标记为下载中并记录开始时间
    WsUtil.sendMsg();
  }

  @Override
  public void onChunkStart(TaskInfo taskInfo, ChunkInfo chunkInfo) {

  }

  @Override
  public void onProgress(TaskInfo taskInfo, ChunkInfo chunkInfo) {

  }

  @Override
  public void onPause(TaskInfo taskInfo) {
    synchronized (taskInfo) {
      taskInfo.setStatus(4);
      for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
        synchronized (chunkInfo) {
          HttpDownUtil.safeClose(chunkInfo.getChannel(), chunkInfo.getFileChannel());
          if (chunkInfo.getStatus() != 2) {
            chunkInfo.setStatus(4);
          }
        }
      }
    }
    WsUtil.sendMsg();
  }

  @Override
  public void onContinue(TaskInfo taskInfo) {
    try {
      synchronized (taskInfo) {
        taskInfo.setStatus(1);
        taskInfo.setPauseTime(
            taskInfo.getPauseTime() + (System.currentTimeMillis() - taskInfo.getLastTime()));
        for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
          synchronized (chunkInfo) {
            if(chunkInfo.getStatus()==4){
              chunkInfo.setPauseTime(taskInfo.getPauseTime());
              HttpDownUtil.retryDown(taskInfo, chunkInfo);
            }
          }
        }
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("call onContinue:" + e);
    }
    WsUtil.sendMsg();
  }

  @Override
  public void onError(TaskInfo taskInfo, ChunkInfo chunkInfo, Throwable cause) {
    try {
      HttpDownUtil.retryDown(taskInfo, chunkInfo);
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("call onError:" + e);
    }
  }

  @Override
  public void onChunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo) {
    WsUtil.sendMsg();
  }

  @Override
  public void onDone(TaskInfo taskInfo) {
    try {
      //更改任务下载状态为已完成
      ByteUtil.serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
      //删除任务进度记录文件
      synchronized (taskInfo) {
        Files.deleteIfExists(Paths.get(taskInfo.buildTaskFilePath() + ".inf"));
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.error("call onDone:" + e);
    }
    WsUtil.sendMsg();
    //检查是否为损坏文件
  }

  @Override
  public void onDelete(TaskInfo taskInfo) {
    for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
      synchronized (chunkInfo) {
        HttpDownUtil.safeClose(chunkInfo.getChannel(), chunkInfo.getFileChannel());
      }
    }
    synchronized (taskInfo) {
      HttpDownServer.RECORD_CONTENT.remove(taskInfo.getId());
      HttpDownServer.DOWN_CONTENT.remove(taskInfo.getId());
      try {
        ByteUtil
            .serialize((Serializable) HttpDownServer.RECORD_CONTENT, HttpDownServer.RECORD_PATH);
        FileUtil.deleteIfExists(taskInfo.buildTaskFilePath());
        FileUtil.deleteIfExists(taskInfo.buildTaskFilePath() + ".inf");
      } catch (IOException e) {
        HttpDownServer.LOGGER.error("call onDelete:" + e);
      }
    }
    WsUtil.sendMsg();
  }
}
