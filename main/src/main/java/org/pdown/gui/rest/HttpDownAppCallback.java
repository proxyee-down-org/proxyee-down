package org.pdown.gui.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.ExtensionInfo;
import org.pdown.gui.extension.HookScript;
import org.pdown.gui.extension.util.ExtensionUtil;
import org.pdown.gui.http.controller.NativeController;
import org.pdown.gui.http.util.HttpHandlerUtil;
import org.pdown.rest.controller.HttpDownRestCallback;
import org.pdown.rest.entity.DownInfo;
import org.pdown.rest.form.CreateTaskForm;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.ResolveForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class HttpDownAppCallback extends HttpDownRestCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownAppCallback.class);

  @Override
  public void onStart(HttpDownBootstrap httpDownBootstrap) {
    super.onStart(httpDownBootstrap);
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
    super.onResume(httpDownBootstrap);
  }

  @Override
  public void onError(HttpDownBootstrap httpDownBootstrap) {
    super.onError(httpDownBootstrap);
    DownInfo downInfo = findDownInfo(httpDownBootstrap);
    if (downInfo != null) {
      //遍历扩展模块是否有对应的处理
      List<ExtensionInfo> extensionInfos = ExtensionContent.get();
      Map<String, Object> taskForm = new HashMap<>();
      taskForm.put("id", downInfo.getId());
      taskForm.put("data", downInfo.getData());
      taskForm.put("request", HttpRequestForm.parse(downInfo.getBootstrap().getRequest()));
      taskForm.put("response", downInfo.getBootstrap().getResponse());
      for (ExtensionInfo extensionInfo : extensionInfos) {
        if (extensionInfo.getMeta().isEnabled()) {
          if (extensionInfo.getHookScript() != null
              && !StringUtils.isEmpty(extensionInfo.getHookScript().getScript())
              && extensionInfo.getHookScript().hasEvent(HookScript.EVENT_ERROR, HttpDownUtil.getUrl(httpDownBootstrap.getRequest()))) {
            try {
              //初始化js引擎
              ScriptEngine engine = ExtensionUtil.buildExtensionRuntimeEngine(extensionInfo);
              Invocable invocable = (Invocable) engine;
              //执行error方法
              invocable.invokeFunction(HookScript.EVENT_ERROR, taskForm);
            } catch (Exception e) {
              LOGGER.error("An exception occurred while error()", e);
            }
          }
        }
      }
    }
  }

  @Override
  public void onProgress(HttpDownBootstrap httpDownBootstrap) {
    super.onProgress(httpDownBootstrap);
  }

  @Override
  public void onDone(HttpDownBootstrap httpDownBootstrap) {
    super.onDone(httpDownBootstrap);
  }

}
