package lee.study.down.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lee.study.down.HttpDownServer;
import lee.study.down.dispatch.DefaultHttpDownCallback;
import lee.study.down.form.DownForm;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.HttpDownUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class HttpDownController {

  @RequestMapping("/getTask")
  @ResponseBody
  public TaskInfo getTask(@RequestParam String id) {
    HttpDownInfo httpDownInfo = HttpDownServer.DOWN_CONTENT.get(id);
    return httpDownInfo == null ? null : httpDownInfo.getTaskInfo();
  }

  @RequestMapping("/getTaskList")
  @ResponseBody
  public List<TaskInfo> getTaskList() {
    List<TaskInfo> taskInfoList = null;
    if (HttpDownServer.DOWN_CONTENT != null && HttpDownServer.DOWN_CONTENT.size() > 0) {
      taskInfoList = new ArrayList<>();
      for (Object key : HttpDownServer.DOWN_CONTENT.keySet().stream().sorted().toArray()) {
        HttpDownInfo httpDownModel = HttpDownServer.DOWN_CONTENT.get(key);
        if (httpDownModel.getTaskInfo().getStatus() != 0) {
          taskInfoList.add(httpDownModel.getTaskInfo());
        }
      }
    }
    return taskInfoList;
  }

  @RequestMapping("/startTask")
  @ResponseBody
  public Map<String, String> startTask(@RequestBody DownForm downForm) {
    Map<String, String> map = new HashMap<>();
    map.put("result", "N");
    if (downForm.getPath() != null && !"".equals(downForm.getPath().trim())) {
      File file = new File(downForm.getPath());
      if (file.exists()) {
        try {
          HttpDownInfo httpDownModel = HttpDownServer.DOWN_CONTENT.get(downForm.getId());
          TaskInfo taskInfo = httpDownModel.getTaskInfo();
          taskInfo.setFilePath(downForm.getPath());
          List<ChunkInfo> chunkInfoList = new ArrayList<>();
          if (taskInfo.getTotalSize() > 0) {  //非chunked编码
            if (taskInfo.isSupportRange()) {
              taskInfo.setConnections(downForm.getConnections());
            }
            //计算chunk列表
            for (int i = 0; i < downForm.getConnections(); i++) {
              ChunkInfo chunkInfo = new ChunkInfo();
              chunkInfo.setIndex(i);
              long chunkSize = taskInfo.getTotalSize() / downForm.getConnections();
              chunkInfo.setOriStartPosition(i * chunkSize);
              chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition());
              if (i == downForm.getConnections() - 1) { //最后一个连接去下载多出来的字节
                chunkSize += taskInfo.getTotalSize() % downForm.getConnections();
              }
              chunkInfo.setEndPosition(chunkInfo.getOriStartPosition() + chunkSize - 1);
              chunkInfo.setTotalSize(chunkSize);
              chunkInfoList.add(chunkInfo);
            }
          } else { //chunked下载
            ChunkInfo chunkInfo = new ChunkInfo();
            chunkInfo.setIndex(0);
            chunkInfo.setNowStartPosition(0);
            chunkInfo.setOriStartPosition(0);
            chunkInfoList.add(chunkInfo);
          }
          taskInfo.setChunkInfoList(chunkInfoList);
          HttpDownUtil.taskDown(httpDownModel, new DefaultHttpDownCallback());
        } catch (Exception e) {
          e.printStackTrace();
          map.put("msg", "服务器异常！");
        }
        map.put("result", "Y");
      } else {
        map.put("msg", "路径不存在！");
      }
    } else {
      map.put("msg", "路径不能为空！");
    }
    return map;
  }

  public static void main(String[] args) {
    new File(".#1 asd");
  }
}
