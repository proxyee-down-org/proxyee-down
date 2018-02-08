package lee.study.down.exception;

import java.io.IOException;

public class BootstrapException extends IOException {

  public BootstrapException() {
    super();
  }

  public BootstrapException(String message) {
    super(message);
  }

  public BootstrapException(String message, Throwable cause) {
    super(message, cause);
  }

  public BootstrapException(Throwable cause) {
    super(cause);
  }
}
