package lee.study.down.update;

import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.model.UpdateInfo;

public interface UpdateService {

  UpdateInfo check(float currVersion) throws Exception;

  AbstractHttpDownBootstrap update(UpdateInfo updateInfo) throws Exception;
}
