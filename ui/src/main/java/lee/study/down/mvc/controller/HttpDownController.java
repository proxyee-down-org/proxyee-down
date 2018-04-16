package lee.study.down.mvc.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.ContentManager;
import lee.study.down.content.DownContent;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.exception.BootstrapException;
import lee.study.down.gui.HttpDownApplication;
import lee.study.down.io.BdyZip;
import lee.study.down.io.BdyZip.BdyUnzipCallback;
import lee.study.down.io.BdyZip.BdyZipEntry;
import lee.study.down.model.ConfigInfo;
import lee.study.down.model.DirInfo;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import lee.study.down.model.TaskInfo;
import lee.study.down.model.UnzipInfo;
import lee.study.down.model.UpdateInfo;
import lee.study.down.mvc.form.BuildTaskForm;
import lee.study.down.mvc.form.ConfigForm;
import lee.study.down.mvc.form.DirForm;
import lee.study.down.mvc.form.NewTaskForm;
import lee.study.down.mvc.form.UnzipForm;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;
import lee.study.down.update.GithubUpdateService;
import lee.study.down.update.UpdateService;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;
import lee.study.down.util.OsUtil;
import lee.study.proxyee.proxy.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HttpDownController {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownController.class);

  @Value("${app.version}")
  private float version;

  @RequestMapping("/getTask")
  public ResultInfo getTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    HttpDownInfo httpDownInfo = ContentManager.DOWN.getDownInfo(id);
    if (httpDownInfo != null) {
      TaskInfo taskInfo = httpDownInfo.getTaskInfo();
      Map<String, Object> data = new HashMap<>();
      data.put("task", NewTaskForm.parse(httpDownInfo));
      //检查是否有相同大小的文件
      List<HttpDownInfo> sameTasks = ContentManager.DOWN.getDownInfos().stream()
          .filter(downInfo -> HttpDownStatus.WAIT != downInfo.getTaskInfo().getStatus()
              && HttpDownStatus.DONE != downInfo.getTaskInfo().getStatus()
              && downInfo.getTaskInfo().getTotalSize() == taskInfo.getTotalSize()
          ).collect(Collectors.toList());
      data.put("sameTasks", NewTaskForm.parse(sameTasks));
      resultInfo.setData(data);
    }
    return resultInfo;
  }

  @RequestMapping("/getStartTasks")
  public ResultInfo getStartTasks() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setData(DownContent.setUrl(ContentManager.DOWN.getStartTasks()));
    return resultInfo;
  }

  @RequestMapping("/startTask")
  public ResultInfo startTask(@RequestBody NewTaskForm taskForm) throws Exception {
    return commonStartTask(taskForm);
  }

  @RequestMapping("/getChildDirList")
  public ResultInfo getChildDirList(@RequestBody(required = false) DirForm body) {
    ResultInfo resultInfo = new ResultInfo();
    List<DirInfo> data = new LinkedList<>();
    resultInfo.setData(data);
    File[] files;
    if (body == null || StringUtils.isEmpty(body.getPath())) {
      if (OsUtil.isMac()) {
        files = new File("/Users")
            .listFiles(file -> file.isDirectory() && file.getName().indexOf(".") != 0);
      } else {
        files = File.listRoots();
      }
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
        } else if (tempFile.isDirectory()
            && (tempFile.getParent() == null || !tempFile.isHidden())
            && (OsUtil.isWindows() || tempFile.getName().indexOf(".") != 0)) {
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
    AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.pauseDown();
    }
    return resultInfo;
  }

  @RequestMapping("/pauseAllTask")
  public ResultInfo pauseAllTask(@RequestBody List<String> taskIds) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (taskIds != null && taskIds.size() > 0) {
      for (String taskId : taskIds) {
        AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(taskId);
        if (bootstrap != null) {
          bootstrap.pauseDown();
        }
      }
    }
    return resultInfo;
  }

  @RequestMapping("/continueTask")
  public ResultInfo continueTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.continueDown();
    }
    return resultInfo;
  }

  @RequestMapping("/continueAllTask")
  public ResultInfo continueAllTask(@RequestBody List<String> taskIds) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (taskIds != null && taskIds.size() > 0) {
      for (String taskId : taskIds) {
        AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(taskId);
        if (bootstrap != null) {
          bootstrap.continueDown();
        }
      }
    }
    return resultInfo;
  }

  @RequestMapping("/deleteTask")
  public ResultInfo deleteTask(@RequestParam String id, @RequestParam boolean delFile)
      throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(id);
    if (bootstrap == null) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    } else {
      bootstrap.delete(delFile);
    }
    return resultInfo;
  }

  @RequestMapping("/deleteAllTask")
  public ResultInfo deleteAllTask(@RequestBody List<String> taskIds, @RequestParam boolean delFile)
      throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (taskIds != null && taskIds.size() > 0) {
      for (String taskId : taskIds) {
        AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(taskId);
        if (bootstrap != null) {
          bootstrap.delete(delFile);
        }
      }
    }
    return resultInfo;
  }

  @RequestMapping("/bdyUnzip")
  public ResultInfo bdyUnzip(@RequestParam String id, @RequestParam boolean ignore,
      @RequestBody UnzipForm unzipForm)
      throws IOException {
    ResultInfo resultInfo = new ResultInfo();
    File file = new File(unzipForm.getFilePath());
    if (file.exists() && file.isFile()) {
      if (!unzipForm.getFilePath().equalsIgnoreCase(unzipForm.getToPath())) {
        if (ignore || BdyZip.isBdyZip(unzipForm.getFilePath())) {
          UnzipInfo unzipInfo = new UnzipInfo().setId(id);
          if (!FileUtil.exists(unzipForm.getToPath())) {
            FileUtil.createDirSmart(unzipForm.getToPath());
          }
          if (!FileUtil.canWrite(unzipForm.getToPath())) {
            resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("无权访问解压路径，请修改路径或开放目录写入权限");
            return resultInfo;
          }
          new Thread(() -> {
            try {
              BdyZip
                  .unzip(unzipForm.getFilePath(), unzipForm.getToPath(), new BdyUnzipCallback() {

                    @Override
                    public void onStart() {
                      unzipInfo.setType(BdyZip.ON_START)
                          .setStartTime(System.currentTimeMillis());
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }

                    @Override
                    public void onFix(long totalSize, long fixSize) {
                      unzipInfo.setType(BdyZip.ON_FIX)
                          .setTotalFixSize(totalSize)
                          .setFixSize(fixSize);
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }

                    @Override
                    public void onFixDone(List<BdyZipEntry> list) {
                      unzipInfo.setType(BdyZip.ON_FIX_DONE)
                          .setTotalFileSize(list.stream().map(entry -> entry.getCompressedSize())
                              .reduce((s1, s2) -> s1 + s2).get());
                    }

                    @Override
                    public void onEntryStart(BdyZipEntry entry) {
                      unzipInfo.setType(BdyZip.ON_ENTRY_START)
                          .setEntry(entry)
                          .setCurrFileSize(entry.getCompressedSize())
                          .setCurrWriteSize(0);
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }

                    @Override
                    public void onEntryWrite(long totalSize, long writeSize) {
                      unzipInfo.setType(BdyZip.ON_ENTRY_WRITE)
                          .setCurrWriteSize(unzipInfo.getCurrWriteSize() + writeSize)
                          .setTotalWriteSize(unzipInfo.getTotalWriteSize() + writeSize);
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }

                    @Override
                    public void onDone() {
                      unzipInfo.setType(BdyZip.ON_DONE)
                          .setEndTime(System.currentTimeMillis());
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }

                    @Override
                    public void onError(Exception e) {
                      unzipInfo.setType(BdyZip.ON_ERROR)
                          .setErrorMsg(e.toString());
                      ContentManager.WS.sendMsg(new WsForm(WsDataType.UNZIP_ING, unzipInfo));
                    }
                  });
            } catch (Exception e) {
              LOGGER.error("unzip error:", e);
            }
          }).start();
        } else {
          resultInfo.setStatus(ResultStatus.BAD.getCode());
          resultInfo.setMsg("解压失败，请确认是否为百度云批量下载zip文件");
        }
      } else {
        resultInfo.setStatus(ResultStatus.BAD.getCode());
        resultInfo.setMsg("解压失败，文件路径与解压路径相同");
      }
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
        HttpDownApplication.getProxyServer().close();
        HttpDownApplication.getProxyServer().setProxyConfig(configInfo.getSecProxyConfig());
        HttpDownApplication.getProxyServer().start(configInfo.getProxyPort());
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

  public static volatile AbstractHttpDownBootstrap updateBootstrap;
  private static volatile UpdateInfo updateInfo;
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
    if (updateBootstrap != null) {
      updateBootstrap.close();
      updateBootstrap = null;
    }
    try {
      updateBootstrap = updateService.update(updateInfo, new HttpDownCallback() {
        @Override
        public void onDone(HttpDownInfo httpDownInfo) throws Exception {
          String zipPath = httpDownInfo.getTaskInfo().buildTaskFilePath();
          String unzipDir = "proxyee-down-" + updateInfo.getVersionStr();
          String unzipPath = unzipDir + "/main/proxyee-down-core.jar";
          //下载完解压
          FileUtil.unzip(zipPath, null, unzipPath);
          //复制出来
          Files.copy(
              Paths.get(httpDownInfo.getTaskInfo().getFilePath() + File.separator + unzipPath),
              Paths.get(httpDownInfo.getTaskInfo().getFilePath() + File.separator
                  + "proxyee-down-core.jar.bak"));
          //删除临时的文件
          FileUtil
              .deleteIfExists(zipPath);
          FileUtil
              .deleteIfExists(
                  httpDownInfo.getTaskInfo().getFilePath() + File.separator + unzipDir);
          //通知客户端
          ContentManager.WS
              .sendMsg(new WsForm(WsDataType.UPDATE_PROGRESS, httpDownInfo.getTaskInfo()));
          //清空更新下载信息
          updateBootstrap = null;
        }
      });
    } catch (TimeoutException e) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("检测更新超时，请重试");
      return resultInfo;
    }
    return resultInfo;
  }

  @RequestMapping("/restart")
  public ResultInfo restart() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    //通知父进程重启
    System.out.println("proxyee-down-update");
    return resultInfo;
  }

  @RequestMapping("/buildTask")
  public ResultInfo buildTask(@RequestBody BuildTaskForm form) throws Exception {
    return commonBuildTask(form);
  }

  @RequestMapping("/getNewTask")
  public ResultInfo getNewTask() throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    TaskInfo taskInfo = ContentManager.DOWN.getWaitTask();
    if (taskInfo != null) {
      resultInfo.setData(taskInfo.getId());
    }
    return resultInfo;
  }

  @RequestMapping("/delNewTask")
  public ResultInfo delNewTask(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    ContentManager.DOWN.removeBoot(id);
    return resultInfo;
  }

  @RequestMapping("/open")
  public ResultInfo open(@RequestParam String url) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    if (ContentManager.CONFIG.get().getUiModel() == 1) {
      OsUtil.openBrowse(url);
    }
    resultInfo.setData(ContentManager.CONFIG.get().getUiModel());
    return resultInfo;
  }

  @RequestMapping("/openTaskDir")
  public ResultInfo openTaskDir(@RequestParam String id) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    TaskInfo taskInfo = ContentManager.DOWN.getTaskInfo(id);
    if (taskInfo != null) {
      if (FileUtil.exists(taskInfo.getFilePath())) {
        Desktop.getDesktop().open(new File(taskInfo.getFilePath()));
      } else {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("目录不存在");
      }
    } else {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
    }
    return resultInfo;
  }

  public static ResultInfo commonBuildTask(BuildTaskForm form) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    Map<String, String> heads = new LinkedHashMap<>();
    if (form.getHeads() != null) {
      for (Map<String, String> head : form.getHeads()) {
        String key = head.get("key");
        String value = head.get("value");
        if (!StringUtils.isEmpty(head.get("key")) && !StringUtils.isEmpty(head.get("value"))) {
          heads.put(key, value);
        }
      }
    }
    try {
      HttpRequestInfo requestInfo = HttpDownUtil
          .buildGetRequest(form.getUrl(), heads, form.getBody());
      TaskInfo taskInfo = HttpDownUtil
          .getTaskInfo(requestInfo,
              null,
              ContentManager.CONFIG.get().getSecProxyConfig(),
              HttpDownConstant.clientSslContext,
              HttpDownConstant.clientLoopGroup);
      HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, requestInfo,
          ContentManager.CONFIG.get().getSecProxyConfig());
      ContentManager.DOWN.putBoot(httpDownInfo);
      resultInfo.setData(taskInfo.getId());
    } catch (MalformedURLException e) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("链接格式不正确");
    } catch (TimeoutException e) {
      resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("连接超时，请重试");
    } catch (Exception e) {
      throw new RuntimeException("buildTask error:" + form.toString(), e);
    }
    return resultInfo;
  }

  public static ResultInfo commonStartTask(NewTaskForm taskForm) throws Exception {
    ResultInfo resultInfo = new ResultInfo();
    AbstractHttpDownBootstrap bootstrap = ContentManager.DOWN.getBoot(taskForm.getId());
    HttpDownInfo httpDownInfo = bootstrap.getHttpDownInfo();
    //覆盖下载
    if (!StringUtils.isEmpty(taskForm.getOldId())) {
      AbstractHttpDownBootstrap oldBootstrap = ContentManager.DOWN.getBoot(taskForm.getOldId());
      if (oldBootstrap == null) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务不存在");
        return resultInfo;
      } else {
        //暂停之前的下载任务
        oldBootstrap.pauseDown();
        //修改request
        oldBootstrap.getHttpDownInfo().setRequest(httpDownInfo.getRequest());
        oldBootstrap.getHttpDownInfo().setProxyConfig(httpDownInfo.getProxyConfig());
        Map<String, Object> attr = oldBootstrap.getHttpDownInfo().getAttrs();
        if (attr == null) {
          attr = new HashMap<>();
          oldBootstrap.getHttpDownInfo().setAttrs(attr);
        }
        attr.put(NewTaskForm.KEY_UNZIP_FLAG, taskForm.isUnzip());
        attr.put(NewTaskForm.KEY_UNZIP_PATH, taskForm.getUnzipPath());
        //移除新的下载任务
        ContentManager.DOWN.removeBoot(taskForm.getId());
        //持久化
        ContentManager.DOWN.save();
        //用新链接继续下载
        oldBootstrap.continueDown();
      }
    } else {
      if (StringUtils.isEmpty(taskForm.getFileName())) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("文件名不能为空");
        return resultInfo;
      }
      if (StringUtils.isEmpty(taskForm.getFilePath())) {
        resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("路径不能为空");
        return resultInfo;
      }
      buildDefaultValues(taskForm);
      TaskInfo taskInfo = httpDownInfo.getTaskInfo();
      synchronized (taskInfo) {
        if (taskInfo.getStatus() != HttpDownStatus.WAIT) {
          resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg("任务已添加至下载列表");
        }
        taskInfo.setFileName(taskForm.getFileName());
        taskInfo.setFilePath(taskForm.getFilePath());
        Map<String, Object> attr = httpDownInfo.getAttrs();
        if (attr == null) {
          attr = new HashMap<>();
          httpDownInfo.setAttrs(attr);
        }
        attr.put(NewTaskForm.KEY_UNZIP_FLAG, taskForm.isUnzip());
        attr.put(NewTaskForm.KEY_UNZIP_PATH, taskForm.getUnzipPath());
        if (taskInfo.isSupportRange()) {
          taskInfo.setConnections(taskForm.getConnections());
        } else {
          taskInfo.setConnections(1);
        }
        try {
          bootstrap.startDown();
        } catch (BootstrapException e) {
          resultInfo.setStatus(ResultStatus.BAD.getCode()).setMsg(e.getMessage());
          return resultInfo;
        }
        //记录存储路径
        String lastPath = ContentManager.CONFIG.get().getLastPath();
        if (!taskForm.getFilePath().equalsIgnoreCase(lastPath)) {
          ContentManager.CONFIG.get().setLastPath(taskForm.getFilePath());
          ContentManager.CONFIG.save();
        }
      }
    }
    return resultInfo;
  }

  private static void buildDefaultValues(NewTaskForm taskForm) {
    //默认分段数
    if (taskForm.getConnections() <= 0) {
      taskForm.setConnections(ContentManager.CONFIG.get().getConnections());
    }
    //默认解压路径
    if (taskForm.isUnzip()) {
      if (StringUtils.isEmpty(taskForm.getUnzipPath())) {
        int index = taskForm.getFileName().lastIndexOf(".");
        if (index != -1) {
          taskForm.setUnzipPath(
              taskForm.getFilePath() + File.separator + taskForm.getFileName().substring(0, index));
        } else {
          taskForm.setUnzipPath(
              taskForm.getFilePath() + File.separator + taskForm.getFileName() + "_unzip");
        }
      }
    }
  }

}
