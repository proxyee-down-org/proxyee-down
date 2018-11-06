package org.pdown.gui.extension.jsruntime.polyfill;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import jdk.internal.dynalink.beans.StaticClass;
import org.pdown.gui.extension.jsruntime.polyfill.property.Console;
import org.pdown.gui.extension.jsruntime.polyfill.property.Document;

public class Window {

  private static final String TIMEOUT_THREAD_NAME = "babel4j-timeout-";
  private static final String INTERVAL_THREAD_NAME = "babel4j-interval-";
  private static final AtomicLong THREAD_ID = new AtomicLong(0);

  public Console console = new Console();
  public Document document = new Document();
  public StaticClass XMLHttpRequest = StaticClass.forClass(org.pdown.gui.extension.jsruntime.polyfill.property.XMLHttpRequest.class);

  public long setTimeout(Function function, long timeout) {
    Long id = THREAD_ID.addAndGet(1);
    new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(timeout);
        function.apply(null);
      } catch (InterruptedException e) {
      }
    }, TIMEOUT_THREAD_NAME + id).start();
    return id;
  }

  public void clearTimeout(Long id) {
    Thread temp = Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> (TIMEOUT_THREAD_NAME + id).equals(thread.getName()))
        .findFirst()
        .orElse(null);
    if (temp != null) {
      temp.interrupt();
    }
  }

  public long setInterval(Function function, long timeout) {
    Long id = THREAD_ID.addAndGet(1);
    new Thread(() -> {
      try {
        while (true) {
          TimeUnit.MILLISECONDS.sleep(timeout);
          function.apply(null);
        }
      } catch (InterruptedException e) {
      }
    }, INTERVAL_THREAD_NAME + id).start();
    return id;
  }

  public void clearInterval(Long id) {
    Thread temp = Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> (INTERVAL_THREAD_NAME + id).equals(thread.getName()))
        .findFirst()
        .orElse(null);
    if (temp != null) {
      temp.interrupt();
    }
  }
}
