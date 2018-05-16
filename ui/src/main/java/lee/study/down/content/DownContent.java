package lee.study.down.content;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.boot.HttpDownBootstrapFactory;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.mvc.form.TaskInfoForm;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class DownContent {

  private final static Logger LOGGER = LoggerFactory.getLogger(DownContent.class);

  //下载对象管理
  private static Map<String, AbstractHttpDownBootstrap> downContent;

  public HttpDownInfo getDownInfo(String id) {
    if (downContent.containsKey(id)) {
      return downContent.get(id).getHttpDownInfo();
    }
    return null;
  }

  public List<HttpDownInfo> getDownInfos() {
    List<HttpDownInfo> httpDownInfoList = new ArrayList<>();
    for (String id : downContent.keySet()) {
      HttpDownInfo httpDownInfo = getDownInfo(id);
      httpDownInfoList.add(httpDownInfo);
    }
    return httpDownInfoList;
  }

  public TaskInfo getTaskInfo(String id) {
    HttpDownInfo httpDownInfo = getDownInfo(id);
    if (httpDownInfo != null) {
      return httpDownInfo.getTaskInfo();
    }
    return null;
  }

  public List<TaskInfo> getStartTasks() {
    List<TaskInfo> taskInfoList = new ArrayList<>();
    for (String id : downContent.keySet()) {
      TaskInfo taskInfo = getTaskInfo(id);
      if (taskInfo != null && taskInfo.getStatus() != HttpDownStatus.WAIT) {
        taskInfoList.add(taskInfo);
      }
    }
    return taskInfoList;
  }

  public List<TaskInfo> getDowningTasks() {
    List<TaskInfo> taskInfoList = new ArrayList<>();
    for (String id : downContent.keySet()) {
      TaskInfo taskInfo = getTaskInfo(id);
      if (taskInfo != null && taskInfo.getStatus() == HttpDownStatus.RUNNING) {
        taskInfoList.add(taskInfo);
      }
    }
    return taskInfoList;
  }

  public TaskInfo getWaitTask() {
    for (String id : downContent.keySet()) {
      TaskInfo taskInfo = getTaskInfo(id);
      if (taskInfo != null && taskInfo.getStatus() == HttpDownStatus.WAIT) {
        return taskInfo;
      }
    }
    return null;
  }

  public WsForm buildWsForm(String taskId) {
    List<TaskInfo> list = new ArrayList<>();
    TaskInfo taskInfo = getTaskInfo(taskId);
    if (taskInfo == null) {
      return null;
    } else {
      list.add(taskInfo);
      return new WsForm(WsDataType.TASK_LIST, setUrl(list));
    }
  }

  public WsForm buildDowningWsForm() {
    List<TaskInfo> list = getDowningTasks();
    if (list == null || list.size() == 0) {
      return null;
    } else {
      return new WsForm(WsDataType.TASK_LIST, setUrl(list));
    }
  }

  public static List<TaskInfoForm> setUrl(List<TaskInfo> taskInfoList) {
    List<TaskInfoForm> ret = new ArrayList<>();
    for (TaskInfo taskInfo : taskInfoList) {
      TaskInfoForm taskInfoForm = new TaskInfoForm();
      BeanUtils.copyProperties(taskInfo, taskInfoForm);
      HttpRequestInfo httpRequest = (HttpRequestInfo) ContentManager.DOWN
          .getDownInfo(taskInfo.getId()).getRequest();
      String uri = httpRequest.uri();
      String host = httpRequest.requestProto().getHost();
      String url = (uri.indexOf("/") == 0 ? host : "") + uri;
      if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) {
        url = (httpRequest.requestProto().getSsl() ? "https://" : "http://") + url;
      }
      taskInfoForm.setUrl(url);
      ret.add(taskInfoForm);
    }
    return ret;
  }

  public void putBoot(AbstractHttpDownBootstrap bootstrap) {
    synchronized (downContent) {
      if (bootstrap.getHttpDownInfo().getTaskInfo().getStatus() == HttpDownStatus.WAIT) {
        TaskInfo taskInfo = getWaitTask();
        if (taskInfo != null) {
          downContent.remove(taskInfo.getId());
        }
      }
      downContent.put(bootstrap.getHttpDownInfo().getTaskInfo().getId(), bootstrap);
    }
  }

  public void putBoot(HttpDownInfo httpDownInfo) {
    AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo,
        ContentManager.CONFIG.get().getRetryCount(),
        HttpDownConstant.clientSslContext,
        HttpDownConstant.httpDownCallback);
    TaskInfo taskInfo = bootstrap.getHttpDownInfo().getTaskInfo();
    if (taskInfo.isSupportRange()) {
      taskInfo.setConnections(ContentManager.CONFIG.get().getConnections());
    }
    taskInfo.setFilePath(ContentManager.CONFIG.get().getLastPath());
    putBoot(bootstrap);
  }

  public void removeBoot(String id) {
    downContent.remove(id);
  }

  public static AbstractHttpDownBootstrap getBoot(String id) {
    return downContent.get(id);
  }

  /**
   * 写入文件
   */
  public void save() {
    try {
      List<HttpDownInfo> httpDownInfo = getDownInfos();
      synchronized (httpDownInfo) {
        ByteUtil.serialize((Serializable) httpDownInfo, HttpDownConstant.TASK_RECORD_PATH);
      }
    } catch (IOException e) {
      LOGGER.error("写入配置文件失败：", e);
    }
  }

  /**
   * 写入文件
   */
  public void saveTask(String id) {
    try {
      TaskInfo taskInfo = getTaskInfo(id);
      if (taskInfo != null) {
        synchronized (taskInfo) {
          ByteUtil.serialize(taskInfo, taskInfo.buildTaskRecordFilePath(),
              taskInfo.buildTaskRecordBakFilePath());
        }
      }
    } catch (IOException e) {
      LOGGER.warn("写入配置文件失败：", e);
    }
  }

  /**
   * 从配置文件中加载信息
   */
  public void init() {
    downContent = new ConcurrentHashMap<>();
    if (FileUtil.exists(HttpDownConstant.TASK_RECORD_PATH)) {
      try {
        List<HttpDownInfo> records = (List<HttpDownInfo>) ByteUtil
            .deserialize(HttpDownConstant.TASK_RECORD_PATH);
        for (HttpDownInfo httpDownInfo : records) {
          AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo,
              ContentManager.CONFIG.get().getRetryCount(),
              HttpDownConstant.clientSslContext,
              HttpDownConstant.httpDownCallback);
          TaskInfo taskInfo = httpDownInfo.getTaskInfo();
          if (taskInfo.getStatus() == HttpDownStatus.WAIT) {
            continue;
          }
          //下载未完成
          if (taskInfo.getStatus() != HttpDownStatus.DONE) {
            String taskDetailPath = taskInfo.buildTaskRecordFilePath();
            String taskDetailBakPath = taskInfo.buildTaskRecordBakFilePath();
            //存在下载进度信息则更新,否则重新下载
            if (FileUtil.existsAny(taskDetailPath, taskDetailBakPath)) {
              try {
                taskInfo = (TaskInfo) ByteUtil.deserialize(taskDetailPath, taskDetailBakPath);
                httpDownInfo.setTaskInfo(taskInfo);
              } catch (IOException | ClassNotFoundException e) {
                taskInfo.reset();
              }
            } else {
              taskInfo.reset();
            }
            if (taskInfo.getStatus() != HttpDownStatus.FAIL) {
              //设置为暂停状态
              taskInfo.setStatus(HttpDownStatus.PAUSE);
              taskInfo.getChunkInfoList().forEach((chunk) -> {
                if (chunk.getStatus() != HttpDownStatus.DONE) {
                  chunk.setStatus(HttpDownStatus.PAUSE);
                }
              });
            }
          }
          putBoot(bootstrap);
        }
      } catch (Exception e) {
        LOGGER.warn("加载配置文件失败：", e);
      }
    }
  }
}
