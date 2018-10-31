package org.pdown.gui.extension;

import java.util.Arrays;

public class HookScript {

  public static final String EVENT_RESOLVE = "resolve";
  public static final String EVENT_ERROR = "error";

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
   * @param event
   * @param url
   * @return
   */
  public boolean hasEvent(String event, String url) {
    String matchUrl = url != null ? url.replaceAll("^(?i)(https?://)", "") : "";
    if (events != null
        && Arrays.stream(events).anyMatch(e -> event.equalsIgnoreCase(e.getOn())
        && (e.getMatches() == null || (Arrays.stream(e.getMatches()).anyMatch(m -> matchUrl.matches(m)))))) {
      return true;
    }
    return false;
  }

  static class Event {

    private String on;
    private String[] matches;

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
  }
}
