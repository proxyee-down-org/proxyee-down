package lee.study.down.update;

import lee.study.down.HttpDownBootstrap;
import lee.study.down.model.UpdateInfo;

public interface UpdateService {

  String UPDATE_FILE_NAME = "proxyee-down-core.jar";

  UpdateInfo check(float currVersion) throws Exception;

  HttpDownBootstrap update(UpdateInfo updateInfo) throws Exception;
}
