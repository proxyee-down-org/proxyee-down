package lee.study.down.dispatch;

import java.util.HashMap;
import lee.study.down.content.ContentManager;
import lee.study.down.io.BdyZip;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.mvc.form.NewTaskForm;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;
import lee.study.down.util.FileUtil;

public class HttpDownHandleCallback extends HttpDownCallback {

  private void sendTask(String id) {
    ContentManager.WS.sendMsg(ContentManager.DOWN.buildWsForm(id));
  }

  @Override
  public void onStart(HttpDownInfo httpDownInfo) throws Exception {
    ContentManager.DOWN.save();
    sendTask(httpDownInfo.getTaskInfo().getId());
  }


  @Override
  public void onPause(HttpDownInfo httpDownInfo) throws Exception {
    sendTask(httpDownInfo.getTaskInfo().getId());
  }

  @Override
  public void onContinue(HttpDownInfo httpDownInfo) throws Exception {
    sendTask(httpDownInfo.getTaskInfo().getId());
  }

  @Override
  public void onError(HttpDownInfo httpDownInfo, Throwable cause) {
    ContentManager.DOWN.saveTask(httpDownInfo.getTaskInfo().getId());
    sendTask(httpDownInfo.getTaskInfo().getId());
  }

  @Override
  public void onChunkDone(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) {
    ContentManager.DOWN.saveTask(httpDownInfo.getTaskInfo().getId());
  }

  @Override
  public void onMerge(HttpDownInfo httpDownInfo) throws Exception {
    ContentManager.DOWN.saveTask(httpDownInfo.getTaskInfo().getId());
    sendTask(httpDownInfo.getTaskInfo().getId());
  }

  @Override
  public void onDone(HttpDownInfo httpDownInfo) throws Exception {
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    //更改任务下载状态为已完成
    ContentManager.DOWN.save();
    //删除任务进度记录文件
    synchronized (taskInfo) {
      FileUtil.deleteIfExists(taskInfo.buildTaskRecordFilePath());
      FileUtil.deleteIfExists(taskInfo.buildTaskRecordBakFilePath());
    }
    sendTask(httpDownInfo.getTaskInfo().getId());
    NewTaskForm taskForm = NewTaskForm.parse(httpDownInfo);
    if (taskForm.isUnzip()) {
      if (BdyZip.isBdyZip(taskInfo.buildTaskFilePath())) {
        WsForm wsForm = new WsForm(WsDataType.UNZIP_NEW, new HashMap<String, String>() {
          {
            put("filePath", taskInfo.buildTaskFilePath());
            put("toPath", taskForm.getUnzipPath());
          }
        });
        ContentManager.WS.sendMsg(wsForm);
      }
    }
  }

  @Override
  public void onDelete(HttpDownInfo httpDownInfo) throws Exception {
    String taskId = httpDownInfo.getTaskInfo().getId();
    ContentManager.DOWN.removeBoot(taskId);
    ContentManager.DOWN.save();
    sendTask(taskId);
  }
}
