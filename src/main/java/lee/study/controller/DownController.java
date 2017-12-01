package lee.study.controller;

import java.util.ArrayList;
import java.util.List;
import lee.study.HttpDownServer;
import lee.study.down.HttpDown;
import lee.study.down.HttpDownCallback;
import lee.study.form.DownForm;
import lee.study.model.ChunkInfo;
import lee.study.model.HttpDownInfo;
import lee.study.model.TaskInfo;
import lee.study.ws.HttpDownProgressHandle;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class DownController {

  @RequestMapping("/getTask")
  @ResponseBody
  public TaskInfo getTask(@RequestParam int id) {
    return HttpDownServer.downContent.get(id).getTaskInfo();
  }

  @RequestMapping("/getTaskList")
  @ResponseBody
  public List<TaskInfo> getTaskList() {
    List<TaskInfo> taskInfoList = null;
    if (HttpDownServer.downContent != null && HttpDownServer.downContent.size() > 0) {
      taskInfoList = new ArrayList<>();
      for (Object key : HttpDownServer.downContent.keySet().stream().sorted().toArray()) {
        HttpDownInfo httpDownModel = HttpDownServer.downContent.get(key);
        if (httpDownModel.getTaskInfo().getStatus() != 0) {
          taskInfoList.add(httpDownModel.getTaskInfo());
        }
      }
    }
    return taskInfoList;
  }

  @RequestMapping("/startTask")
  @ResponseBody
  public String startTask(@RequestBody DownForm downForm) {
    try {
      HttpDownInfo httpDownModel = HttpDownServer.downContent.get(downForm.getId());
      httpDownModel.getTaskInfo().setFilePath(downForm.getPath());
      httpDownModel.getTaskInfo().setConnections(downForm.getConnections());
      HttpDown.fastDown(httpDownModel, downForm.getConnections(),
          HttpDownServer.loopGroup,
          downForm.getPath(), new HttpDownCallback() {

            @Override
            public void start(TaskInfo taskInfo) {
              //标记为下载中并记录开始时间
              taskInfo.setStatus(1);
              taskInfo.setStartTime(System.currentTimeMillis());
              taskInfo.setChunkInfoList(new ArrayList<>());
              HttpDownProgressHandle.sendMsg("start", taskInfo);
            }

            @Override
            public void chunkStart(TaskInfo taskInfo, ChunkInfo chunkInfo) {
              chunkInfo.setStartTime(System.currentTimeMillis());
              taskInfo.getChunkInfoList().add(chunkInfo);
            }

            @Override
            public void progress(TaskInfo taskInfo, ChunkInfo chunkInfo, long chunkDownSize,
                long chunkTotalSize,
                long fileDownSize,
                long fileTotalSize) {
//              System.out.println("总大小:"+fileTotalSize+"\t已下载："+fileDownSize);
//              System.out.println("文件块("+index+")总大小:"+chunkTotalSize+"\t已下载："+chunkDownSize);
              long lastTime = System.currentTimeMillis();
              chunkInfo.setDownSize(chunkDownSize);
              chunkInfo.setLastTime(lastTime);
              taskInfo.setLastTime(lastTime);
              //sendMsg("progress",taskInfo);
            }

            @Override
            public void error(TaskInfo taskInfo, ChunkInfo chunkInfo, Throwable cause) {

            }

            @Override
            public void chunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo) {
              chunkInfo.setStatus(2);
              HttpDownProgressHandle.sendMsg("chunkDone", taskInfo);
            }

            @Override
            public void done(TaskInfo taskInfo) {
              taskInfo.setStatus(2);
              taskInfo.setLastTime(System.currentTimeMillis());
              HttpDownProgressHandle.sendMsg("done", taskInfo);
            }
          });
    } catch (Exception e) {
      e.printStackTrace();
      return "N";
    }
    return "Y";
  }
}
