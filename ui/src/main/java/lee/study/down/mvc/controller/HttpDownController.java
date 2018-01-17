package lee.study.down.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lee.study.down.HttpDownApplication;
import lee.study.down.HttpDownBootstrap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.ContentManager;
import lee.study.down.io.BdyZip;
import lee.study.down.model.ConfigInfo;
import lee.study.down.model.DirInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import lee.study.down.model.TaskInfo;
import lee.study.down.model.UpdateInfo;
import lee.study.down.mvc.form.ConfigForm;
import lee.study.down.mvc.form.DirForm;
import lee.study.down.mvc.form.UnzipForm;
import lee.study.down.update.GithubUpdateService;
import lee.study.down.update.UpdateService;
import lee.study.down.util.FileUtil;
import lee.study.proxyee.proxy.ProxyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HttpDownController {

  @Autowired
  private HttpDownApplication httpDownApplication;

  @Value("${app.version}")
  private float version;

  @RequestMapping("/getTask")
  public ResultInfo getTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    TaskInfo taskInfo = ContentManager.DOWN.getTaskInfo(id);
    if (taskInfo == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      if (taskInfo.isSupportRange()) {
        taskInfo.setConnections(ContentManager.CONFIG.get().getConnections());
      }
      taskInfo.setFilePath(ContentManager.CONFIG.get().getLastPath());
      resultInfo.setData(taskInfo);
    }
    return resultInfo;
  }

  @RequestMapping("/getTaskList")
  public ResultInfo getTaskList() {
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setData(ContentManager.DOWN.getStartTasks());
    return resultInfo;
  }

  @RequestMapping("/startTask")
  public ResultInfo startTask(@RequestBody TaskInfo taskForm) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (StringUtils.isEmpty(taskForm.getFileName())) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("文件名不能为空");
      return resultInfo;
    }
    if (StringUtils.isEmpty(taskForm.getFilePath())) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("路径不能为空");
      return resultInfo;
    }
    HttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(taskForm.getId());
    HttpDownInfo httpDownInfo = bootstrap.getHttpDownInfo();
    TaskInfo taskInfo = httpDownInfo.getTaskInfo();
    synchronized (taskInfo) {
      if (taskInfo.getStatus() != HttpDownStatus.WAIT) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务已添加至下载列表");
      }
      taskInfo.setFileName(taskForm.getFileName());
      taskInfo.setFilePath(taskForm.getFilePath());
      //有文件同名
      if (new File(taskInfo.buildTaskFilePath()).exists()) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("文件名已存在，请修改");
        return resultInfo;
      }
      if (taskInfo.isSupportRange()) {
        taskInfo.setConnections(taskForm.getConnections());
      } else {
        taskInfo.setConnections(1);
      }
      bootstrap.startDown();
      //记录存储路径
      String lastPath = ContentManager.CONFIG.get().getLastPath();
      if (!taskForm.getFilePath().equalsIgnoreCase(lastPath)) {
        ContentManager.CONFIG.get().setLastPath(taskForm.getFilePath());
        ContentManager.CONFIG.save();
      }
    }
    return resultInfo;
  }

  @RequestMapping("/getChildDirList")
  public ResultInfo getChildDirList(@RequestBody(required = false) DirForm body) {
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
      boolean isFileList = "file".equals(body.getModel());
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
    HttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
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
    HttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.continueDown();
    }
    return resultInfo;
  }

  @RequestMapping("/deleteTask")
  public ResultInfo deleteTask(@RequestParam String id, @RequestParam boolean delFile)
      throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    HttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
      bootstrap.close();
      ContentManager.DOWN.removeBoot(id);
      ContentManager.DOWN.save();
      //删除任务进度记录文件
      synchronized (taskInfo) {
        FileUtil.deleteIfExists(taskInfo.buildTaskRecordFilePath());
        if (delFile) {
          FileUtil.deleteIfExists(taskInfo.buildTaskFilePath());
        }
      }
    }
    return resultInfo;
  }

  @RequestMapping("/bdyUnzip")
  public ResultInfo bdyUnzip(@RequestBody UnzipForm unzipForm) throws IOException {
    ResultInfo resultInfo = new ResultInfo();
    File file = new File(unzipForm.getFilePath());
    if (file.exists() && file.isFile()) {
      BdyZip.unzip(unzipForm.getFilePath(), unzipForm.getToPath());
    } else {
      resultInfo.setStatus(ResultStatus.BAD.getCode());
      resultInfo.setMsg("解压失败，文件不存在");
    }
    return resultInfo;
  }


  @RequestMapping("/getConfigInfo")
  public ResultInfo getConfigInfo() {
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setData(ContentManager.CONFIG.get());
    return resultInfo;
  }

  @RequestMapping("/setConfigInfo")
  public ResultInfo setConfigInfo(@RequestBody ConfigForm configForm) {
    ResultInfo resultInfo = new ResultInfo();
    int beforePort = ContentManager.CONFIG.get().getProxyPort();
    boolean beforeSecProxyEnable = ContentManager.CONFIG.get().isSecProxyEnable();
    ProxyConfig beforeSecProxyConfig = ContentManager.CONFIG.get().getSecProxyConfig();
    if (!configForm.isSecProxyEnable()) {
      configForm.setSecProxyConfig(null);
    }
    ConfigInfo configInfo = configForm.convert();
    ContentManager.CONFIG.set(configInfo);
    ContentManager.CONFIG.save();
    //代理服务器需要重新启动
    if (beforePort != configInfo.getProxyPort()
        || beforeSecProxyEnable != configInfo.isSecProxyEnable()
        || (configInfo.isSecProxyEnable() && !configInfo.getSecProxyConfig()
        .equals(beforeSecProxyConfig))
        ) {
      new Thread(() -> {
        httpDownApplication.getProxyServer().close();
        httpDownApplication.getProxyServer().setProxyConfig(configInfo.getSecProxyConfig());
        httpDownApplication.getProxyServer().start(configInfo.getProxyPort());
      }).start();
    }
    return resultInfo;
  }

  @RequestMapping("/getVersion")
  public ResultInfo getVersion() {
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setData(version);
    return resultInfo;
  }

  private static HttpDownBootstrap bootstrap;
  private static volatile UpdateInfo updateInfo;
//  private static final UpdateService updateService = new TestUpdateService();
  private static final UpdateService updateService = new GithubUpdateService();

  @RequestMapping("/checkUpdate")
  public ResultInfo checkUpdate() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    updateInfo = updateService.check(version);
    if (updateInfo == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("已经是最新版本");
      return resultInfo;
    }
    resultInfo.setData(updateInfo);
    return resultInfo;
  }

  @RequestMapping("/doUpdate")
  public ResultInfo doUpdate() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (updateInfo == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("没有可用版本进行更新");
      return resultInfo;
    }
    if (bootstrap != null) {
      bootstrap.close();
      bootstrap = null;
    }
    try {
      bootstrap = updateService.update(updateInfo);
    } catch (TimeoutException e) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("检测更新超时，请重试");
      return resultInfo;
    }
    return resultInfo;
  }

  @RequestMapping("/getUpdateProgress")
  public ResultInfo getUpdateProgress() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (bootstrap != null) {
      resultInfo.setData(bootstrap.getHttpDownInfo().getTaskInfo());
    }
    return resultInfo;
  }

  @RequestMapping("/restart")
  public ResultInfo restart() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    System.out.println("proxyee-down-update");
    return resultInfo;
  }

}
