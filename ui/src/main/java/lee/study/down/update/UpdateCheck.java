package lee.study.down.update;

import lee.study.down.model.UpdateInfo;

public interface UpdateCheck {
  UpdateInfo check() throws Exception;
}
