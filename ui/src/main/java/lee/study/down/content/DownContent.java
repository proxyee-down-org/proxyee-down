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
import lee.study.down.model.TaskInfo;
import lee.study.down.util.ByteUtil;
import lee.study.down.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      if (taskInfo != null && taskInfo.getStatus() != 0) {
        taskInfoList.add(taskInfo);
      }
    }
    return taskInfoList;
  }

  public void putBoot(AbstractHttpDownBootstrap bootstrap) {
    downContent.put(bootstrap.getHttpDownInfo().getTaskInfo().getId(), bootstrap);
  }

  public void putBoot(HttpDownInfo httpDownInfo) {
    AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo,
        ContentManager.CONFIG.get().getRetryCount(),
        HttpDownConstant.clientSslContext,
        HttpDownConstant.clientLoopGroup,
        HttpDownConstant.httpDownCallback);
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
          ByteUtil.serialize(taskInfo, taskInfo.buildTaskRecordFilePath());
        }
      }
    } catch (IOException e) {
      LOGGER.error("写入配置文件失败：", e);
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
              HttpDownConstant.clientLoopGroup,
              HttpDownConstant.httpDownCallback);
          TaskInfo taskInfo = httpDownInfo.getTaskInfo();
          if (taskInfo.getStatus() == HttpDownStatus.WAIT) {
            continue;
          }
          //下载未完成
          if (taskInfo.getStatus() != HttpDownStatus.DONE) {
            String taskDetailPath = taskInfo.buildTaskRecordFilePath();
            //存在下载进度信息则更新,否则重新下载
            if (FileUtil.exists(taskDetailPath)) {
              taskInfo = (TaskInfo) ByteUtil.deserialize(taskDetailPath);
              httpDownInfo.setTaskInfo(taskInfo);
            } else {
              taskInfo.reset();
            }
            if (taskInfo.getStatus() == HttpDownStatus.MERGE) {
              //设置为合并取消状态
              taskInfo.setStatus(HttpDownStatus.MERGE_CANCEL);
            } else if (taskInfo.getStatus() != HttpDownStatus.FAIL) {
              //设置为暂停状态
              taskInfo.setStatus(HttpDownStatus.PAUSE);
              taskInfo.getChunkInfoList().forEach((chunk) -> chunk.setStatus(HttpDownStatus.PAUSE));
            }
          }
          putBoot(bootstrap);
        }
      } catch (Exception e) {
        LOGGER.error("加载配置文件失败：", e);
      }
    }
  }
}
