package lee.study.down.mvc.controller;

import lee.study.down.content.ContentManager;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import lee.study.down.model.TaskInfo;
import lee.study.down.mvc.form.CreateTaskForm;
import lee.study.down.mvc.form.NewTaskForm;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("open")
public class OpenController {

  @RequestMapping("createTask")
  public ResultInfo open(@RequestBody CreateTaskForm createTaskForm) throws Exception {
    ResultInfo resultInfo = HttpDownController.commonBuildTask(createTaskForm.getRequest());
    if (resultInfo.getStatus() == ResultStatus.SUCC.getCode()) {
      TaskInfo taskInfo = ContentManager.DOWN.getTaskInfo(resultInfo.getData().toString());
      NewTaskForm taskForm = new NewTaskForm();
      taskForm.setId(taskInfo.getId());
      if (!StringUtils.isEmpty(createTaskForm.getFileName())) {
        taskForm.setFileName(createTaskForm.getFileName());
      } else {
        taskForm.setFileName(taskInfo.getFileName());
      }
      if(!StringUtils.isEmpty(taskInfo.getFilePath())){
        taskForm.setFilePath(taskInfo.getFilePath());
      }else{
        taskForm.setFilePath(createTaskForm.getFilePath());
      }
      taskForm.setUnzip(createTaskForm.getUnzipFlag() == 1);
      taskForm.setUnzipPath(createTaskForm.getUnzipPath());
      taskForm.setConnections(createTaskForm.getConnections());
      resultInfo = HttpDownController.commonStartTask(taskForm);
    }
    return resultInfo;
  }
}
