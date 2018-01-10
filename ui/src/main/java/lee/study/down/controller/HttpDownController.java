package lee.study.down.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lee.study.down.HttpDownBootstrap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.HttpDownContent;
import lee.study.down.form.DownForm;
import lee.study.down.form.UnzipForm;
import lee.study.down.io.BdyZip;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.DirInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import lee.study.down.model.TaskInfo;
import lee.study.down.util.FileUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HttpDownController {

  @RequestMapping("/getTask")
  public ResultInfo getTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    TaskInfo taskInfo = HttpDownContent.getTaskInfo(id);
    if (taskInfo == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      resultInfo.setData(taskInfo);
    }
    return resultInfo;
  }

  @RequestMapping("/getTaskList")
  public ResultInfo getTaskList() {
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setData(HttpDownContent.getStartTasks());
    return resultInfo;
  }

  @RequestMapping("/startTask")
  public ResultInfo startTask(@RequestBody DownForm downForm) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (StringUtils.isEmpty(downForm.getFileName())) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("文件名不能为空");
      return resultInfo;
    }
    if (StringUtils.isEmpty(downForm.getPath())) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("路径不能为空");
      return resultInfo;
    }
    if (!new File(downForm.getPath()).exists()) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("路径不存在");
      return resultInfo;
    }
    HttpDownBootstrap bootstrap = HttpDownContent.getBoot(downForm.getId());
    HttpDownInfo httpDownInfo = bootstrap.getHttpDownInfo();
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      if (taskInfo.getStatus() != HttpDownStatus.WAIT) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务已添加至下载列表");
      }
      taskInfo.setFileName(downForm.getFileName());
      taskInfo.setFilePath(downForm.getPath());
      //有文件同名
      if (new File(taskInfo.buildTaskFilePath()).exists()) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("文件名已存在，请修改");
        return resultInfo;
      }
      List<ChunkInfo> chunkInfoList = new ArrayList<>();
      if (taskInfo.getTotalSize() > 0) {  //非chunked编码
        if (taskInfo.isSupportRange()) {
          taskInfo.setConnections(downForm.getConnections());
        } else {
          taskInfo.setConnections(1);
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
      bootstrap.startDown();
    }
    return resultInfo;
  }

  @RequestMapping("/getChildDirList")
  public ResultInfo getChildDirList(@RequestParam String model,
      @RequestBody(required = false) DirInfo body) {
    ResultInfo resultInfo = new ResultInfo();
    List<DirInfo> data = new LinkedList<>();
    resultInfo.setData(data);
    File[] files;
    if (body == null || StringUtils.isEmpty(body.getPath())) {
      files = File.listRoots();
    } else {
      File file = new File(body.getPath());
      if (file.exists() && file.isDirectory()) {
        files = file.listFiles();
      } else {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("路径不存在");
        return resultInfo;
      }
    }
    if (files != null && files.length > 0) {
      boolean isFileList = "file".equals(model);
      for (File tempFile : files) {
        if (tempFile.isFile()) {
          if (isFileList) {
            data.add(new DirInfo(
                StringUtils.isEmpty(tempFile.getName()) ? tempFile.getAbsolutePath()
                    : tempFile.getName(), tempFile.getAbsolutePath(), true));
          }
        } else if (tempFile.isDirectory() && (tempFile.getParent() == null || !tempFile
            .isHidden())) {
          DirInfo dirInfo = new DirInfo(
              StringUtils.isEmpty(tempFile.getName()) ? tempFile.getAbsolutePath()
                  : tempFile.getName(), tempFile.getAbsolutePath(),
              tempFile.listFiles() == null ? true : Arrays.stream(tempFile.listFiles())
                  .noneMatch(file ->
                      file != null && (file.isDirectory() || isFileList) && !file.isHidden()
                  ));
          data.add(dirInfo);
        }
      }
    }
    return resultInfo;
  }

  @RequestMapping("/pauseTask")
  public ResultInfo pauseTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    HttpDownBootstrap bootstrap = HttpDownContent.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.pauseDown();
    }
    return resultInfo;
  }

  @RequestMapping("/continueTask")
  public ResultInfo continueTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    HttpDownBootstrap bootstrap = HttpDownContent.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.continueDown();
    }
    return resultInfo;
  }

  @RequestMapping("/deleteTask")
  public ResultInfo deleteTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    HttpDownBootstrap bootstrap = HttpDownContent.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
      bootstrap.close();
      HttpDownContent.removeBoot(id);
      HttpDownContent.save();
      //删除任务进度记录文件
      synchronized (taskInfo) {
        FileUtil.deleteIfExists(taskInfo.buildTaskRecordFilePath());
      }
    }
    return resultInfo;
  }

  @RequestMapping("/bdyUnzip")
  public ResultInfo bdyUnzip(@RequestBody UnzipForm unzipForm) {
    ResultInfo resultInfo = new ResultInfo();
    try {
      BdyZip.unzip(unzipForm.getFilePath(), unzipForm.getToPath());
    } catch (IOException e) {

    }
    return resultInfo;
  }
}
