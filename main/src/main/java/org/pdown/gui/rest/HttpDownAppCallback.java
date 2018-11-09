package org.pdown.gui.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.HookScript;
import org.pdown.gui.extension.HookScript.Event;
import org.pdown.gui.extension.util.ExtensionUtil;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.controller.HttpDownRestCallback;
import org.pdown.rest.entity.DownInfo;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

public class HttpDownAppCallback extends HttpDownRestCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownAppCallback.class);

  @Override
  public void onStart(HttpDownBootstrap httpDownBootstrap) {
    super.onStart(httpDownBootstrap);
    commonHook(httpDownBootstrap, HookScript.EVENT_START, false);
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
    super.onResume(httpDownBootstrap);
    commonHook(httpDownBootstrap, HookScript.EVENT_RESUME, false);
  }

  @Override
  public void onPause(HttpDownBootstrap httpDownBootstrap) {
    super.onPause(httpDownBootstrap);
    commonHook(httpDownBootstrap, HookScript.EVENT_PAUSE, false);
  }

  @Override
  public void onError(HttpDownBootstrap httpDownBootstrap) {
    super.onError(httpDownBootstrap);
    commonHook(httpDownBootstrap, HookScript.EVENT_ERROR, true);
  }

  @Override
  public void onDone(HttpDownBootstrap httpDownBootstrap) {
    super.onDone(httpDownBootstrap);
    commonHook(httpDownBootstrap, HookScript.EVENT_DONE, true);
  }

  private void commonHook(HttpDownBootstrap httpDownBootstrap, String event, boolean async) {
    DownInfo downInfo = findDownInfo(httpDownBootstrap);
    Map<String, Object> taskInfo = buildTaskInfo(downInfo);
    if (taskInfo != null) {
      //遍历扩展模块是否有对应的处理
      List<ExtensionInfo> extensionInfos = ExtensionContent.get();
      for (ExtensionInfo extensionInfo : extensionInfos) {
        if (extensionInfo.getMeta().isEnabled()) {
          if (extensionInfo.getHookScript() != null
              && !StringUtils.isEmpty(extensionInfo.getHookScript().getScript())) {
            Event e = extensionInfo.getHookScript().hasEvent(event, HttpDownUtil.getUrl(httpDownBootstrap.getRequest()));
            if (e != null) {
              try {
                //执行钩子函数
                Object result = ExtensionUtil.invoke(extensionInfo, e, taskInfo, async);
                if (result != null) {
                  ObjectMapper objectMapper = new ObjectMapper();
                  String temp = objectMapper.writeValueAsString(result);
                  TaskForm taskForm = objectMapper.readValue(temp, TaskForm.class);
                  if (taskForm.getRequest() != null) {
                    httpDownBootstrap.setRequest(
                        HttpDownUtil.buildRequest(taskForm.getRequest().getMethod(),
                            taskForm.getRequest().getUrl(),
                            taskForm.getRequest().getHeads(),
                            taskForm.getRequest().getBody())
                    );
                  }
                  if (taskForm.getResponse() != null) {
                    httpDownBootstrap.setResponse(taskForm.getResponse());
                  }
                  if (taskForm.getData() != null) {
                    downInfo.setData(taskForm.getData());
                  }
                  HttpDownContent.getInstance().save();
                }
              } catch (Exception ex) {
                LOGGER.error("An hook exception occurred while " + event + "()", ex);
              }
            }
          }
        }
      }
    }
  }

  private Map<String, Object> buildTaskInfo(DownInfo downInfo) {
    if (downInfo != null) {
      Map<String, Object> taskForm = new HashMap<>();
      taskForm.put("id", downInfo.getId());
      taskForm.put("data", clone(downInfo.getData(), new HashMap<String, Object>()));
      taskForm.put("request", HttpRequestForm.parse(downInfo.getBootstrap().getRequest()));
      taskForm.put("response", clone(downInfo.getBootstrap().getResponse(), new HttpResponseInfo()));
      return taskForm;
    }
    return null;
  }

  private Object clone(Object source, Object target) {
    if (source != null && target != null) {
      BeanUtils.copyProperties(source, target);
      return target;
    }
    return null;
  }

}
