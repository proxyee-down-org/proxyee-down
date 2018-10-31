package org.pdown.gui.extension.jsruntime.polyfill.property;

public class Console {

  public void log(Object object) {
    System.out.println(object);
  }

  public void debug(Object object) {
    System.out.println(object);
  }

  public void error(Object object) {
    if (object instanceof Throwable) {
      Throwable throwable = (Throwable) object;
      throwable.printStackTrace();
    } else {
      System.out.println(object);
    }
  }

  public void error(Object msg, Object throwable) {
    if (throwable instanceof Throwable) {
      ((Throwable) throwable).printStackTrace();
    } else {
      System.out.println(msg);
    }
  }
}
