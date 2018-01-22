package lee.study.down.dispatch;

import lee.study.down.content.ContentManager;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;

public class HttpDownHandleCallback implements HttpDownCallback {

  @Override
  public void onStart(HttpDownInfo httpDownInfo) throws Exception {
    //保存下载记录
    ContentManager.DOWN.save();
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onChunkStart(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {

  }

  @Override
  public void onProgress(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {

  }

  @Override
  public void onPause(HttpDownInfo httpDownInfo) throws Exception {
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onContinue(HttpDownInfo httpDownInfo) throws Exception {
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onError(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo, Throwable cause) {
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onChunkDone(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) {
    ContentManager.DOWN.saveTask(httpDownInfo.getTaskInfo().getId());
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onMerge(HttpDownInfo httpDownInfo) throws Exception {
    ContentManager.DOWN.saveTask(httpDownInfo.getTaskInfo().getId());
    ContentManager.WS.sendMsg();
  }

  @Override
  public void onDone(HttpDownInfo httpDownInfo) throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    //更改任务下载状态为已完成
    ContentManager.DOWN.save();
    //删除任务进度记录文件
    synchronized (taskInfo) {
      FileUtil.deleteIfExists(taskInfo.buildTaskRecordFilePath());
    }
    ContentManager.WS.sendMsg();
  }
}
