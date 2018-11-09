package org.pdown.gui.extension;

import java.util.Arrays;

public class HookScript {

  public static final String EVENT_RESOLVE = "resolve";
  public static final String EVENT_START = "start";
  public static final String EVENT_RESUME = "resume";
  public static final String EVENT_PAUSE = "pause";
  public static final String EVENT_ERROR = "error";
  public static final String EVENT_DONE = "done";
  public static final String EVENT_DELETE = "delete";

  private Event[] events;
  private String script;

  public Event[] getEvents() {
    return events;
  }

  public HookScript setEvents(Event[] events) {
    this.events = events;
    return this;
  }

  public String getScript() {
    return script;
  }

  public HookScript setScript(String script) {
    this.script = script;
    return this;
  }

  /**
   * 判断扩展是否有注册钩子函数
   */
  public Event hasEvent(String event, String url) {
    String matchUrl = url != null ? url.replaceAll("^(?i)(https?://)", "") : "";
    if (events != null) {
      return Arrays.stream(events)
          .filter(e -> event.equalsIgnoreCase(e.getOn()) && (e.getMatches() == null || (Arrays.stream(e.getMatches()).anyMatch(m -> matchUrl.matches(m)))))
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  public static class Event {

    private String on;
    private String[] matches;
    private String method;

    public String getOn() {
      return on;
    }

    public Event setOn(String on) {
      this.on = on;
      return this;
    }

    public String[] getMatches() {
      return matches;
    }

    public Event setMatches(String[] matches) {
      this.matches = matches;
      return this;
    }

    public String getMethod() {
      return method;
    }

    public Event setMethod(String method) {
      this.method = method;
      return this;
    }
  }
}
