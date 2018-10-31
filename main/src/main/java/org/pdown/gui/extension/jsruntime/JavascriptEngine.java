package org.pdown.gui.extension.jsruntime;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.pdown.gui.extension.jsruntime.polyfill.Window;
import org.pdown.rest.form.HttpRequestForm;

public class JavascriptEngine {

  public static ScriptEngine buildEngine() throws ScriptException, NoSuchMethodException {
    NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    ScriptEngine engine = factory.getScriptEngine(new SafeClassFilter());
    Window window = new Window();
    Object global = engine.eval("this");
    Object jsObject = engine.eval("Object");
    Invocable invocable = (Invocable) engine;
    invocable.invokeMethod(jsObject, "bindProperties", global, window);
    engine.eval("var window = this");
    return engine;
  }

  /**
   * 禁止任何显式调用java代码
   */
  private static class SafeClassFilter implements ClassFilter {

    @Override
    public boolean exposeToScripts(String s) {
      return false;
    }
  }

  public static void main(String[] args) throws ScriptException, NoSuchMethodException, JsonProcessingException, InterruptedException {
    ScriptEngine engine = buildEngine();
    Invocable invocable = (Invocable) engine;
    engine.eval("load('E:/study/extensions/bilibili-helper/dist/hook.js')");
    HttpRequestForm requestForm = new HttpRequestForm();
    requestForm.setUrl("https://www.bilibili.com/video/av34765642");
    Object result = invocable.invokeFunction("error");
    ScriptContext ctx = new SimpleScriptContext();
    ctx.setAttribute("result", result, ScriptContext.ENGINE_SCOPE);
    System.out.println(engine.eval("!!result&&typeof result=='object'&&typeof result.then=='function'", ctx));
  }

}
