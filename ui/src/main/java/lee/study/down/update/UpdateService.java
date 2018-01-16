package lee.study.down.update;

import lee.study.down.HttpDownBootstrap;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.UpdateInfo;

public interface UpdateService {
  UpdateInfo check(float currVersion) throws Exception;
  HttpDownBootstrap update(UpdateInfo updateInfo,HttpDownCallback callback) throws Exception;
}
