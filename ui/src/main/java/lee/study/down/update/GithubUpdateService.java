package lee.study.down.update;

import java.util.Collections;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.boot.HttpDownBootstrapFactory;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.TaskInfo;
import lee.study.down.model.UpdateInfo;
import lee.study.down.util.FileUtil;
import lee.study.down.util.HttpDownUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GithubUpdateService implements UpdateService {

  @Override
  public UpdateInfo check(float currVersion) throws Exception {
    UpdateInfo updateInfo = new UpdateInfo();
    Document document = Jsoup.connect("https://github.com/monkeyWie/proxyee-down/releases").get();
    Elements versions = document.select("h1.release-title.text-normal");
    Collections.sort(versions, (v1, v2) -> {
      float version1 = Float.parseFloat(v1.text());
      float version2 = Float.parseFloat(v2.text());
      return version1 < version2 ? 1 : -1;
    });
    float maxVersion = Float.parseFloat(versions.get(0).text());
    if (maxVersion > currVersion) {
      updateInfo.setVersion(maxVersion);
      updateInfo.setVersionStr(versions.get(0).text());
      Element releaseDiv = versions.get(0).parent().parent();
      for (Element element : releaseDiv.select(".d-block.py-2")) {
        if (element.select("strong").text().indexOf("-jar.zip") != -1) {
          updateInfo.setUrl("https://github.com" + element.select("a").attr("href"));
          break;
        }
      }
      if (updateInfo.getUrl() == null) {
        return null;
      }
      updateInfo.setDesc(releaseDiv.select(".markdown-body").html());
      return updateInfo;
    }
    return null;
  }

  @Override
  public AbstractHttpDownBootstrap update(UpdateInfo updateInfo, HttpDownCallback callback)
      throws Exception {
    HttpRequestInfo requestInfo = HttpDownUtil.buildGetRequest(updateInfo.getUrl());
    TaskInfo taskInfo = HttpDownUtil
        .getTaskInfo(requestInfo, null, null, HttpDownConstant.clientSslContext, HttpDownConstant.clientLoopGroup)
        .setConnections(64)
        .setFileName("proxyee-down-jar.zip")
        .setFilePath(HttpDownConstant.HOME_PATH.substring(0, HttpDownConstant.HOME_PATH.length() - 1));
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, requestInfo, null);
    AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo, 5, HttpDownConstant.clientSslContext, callback);
    FileUtil.deleteIfExists(bootstrap.getHttpDownInfo().getTaskInfo().buildTaskFilePath());
    bootstrap.startDown();
    return bootstrap;
  }

  public static void main(String[] args) throws Exception {
    GithubUpdateService githubUpdateService = new GithubUpdateService();
    UpdateInfo updateInfo = githubUpdateService.check(1.0F);
    githubUpdateService.update(updateInfo, null);
  }
}
