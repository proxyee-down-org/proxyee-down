package lee.study.down.update;

import lee.study.down.HttpDownBootstrap;
import lee.study.down.model.UpdateInfo;

public interface UpdateService {

  UpdateInfo check(float currVersion) throws Exception;

  HttpDownBootstrap update(UpdateInfo updateInfo) throws Exception;
}
