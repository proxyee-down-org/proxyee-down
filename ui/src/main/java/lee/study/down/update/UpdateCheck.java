package lee.study.down.update;

import lee.study.down.model.HttpDownInfo;

public interface UpdateCheck {
  HttpDownInfo check() throws Exception;
}
