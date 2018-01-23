package lee.study.down.update;

import io.netty.handler.codec.http.HttpMethod;
import java.util.Collections;
import lee.study.down.boot.AbstractHttpDownBootstrap;
import lee.study.down.boot.HttpDownBootstrapFactory;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.HttpHeadsInfo;
import lee.study.down.model.HttpRequestInfo;
import lee.study.down.model.HttpRequestInfo.HttpVer;
import lee.study.down.model.TaskInfo;
import lee.study.down.model.UpdateInfo;
import lee.study.down.util.HttpDownUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GithubUpdateService implements UpdateService {

  private static final String UPDATE_CORE_FILE_NAME = "proxyee-down-core.jar";
  private static final String HOST = "github.com";

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
      Element releaseDiv = versions.get(0).parent().parent();
      for (Element element : releaseDiv.select(".d-block.py-2")) {
        if (UPDATE_CORE_FILE_NAME.equalsIgnoreCase(element.select("strong").text())) {
          updateInfo.setUrl("https://" + HOST + element.select("a").attr("href"));
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
  public AbstractHttpDownBootstrap update(UpdateInfo updateInfo)
      throws Exception {
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1, HttpMethod.GET.toString(),
        updateInfo.getUrl(), buildHead(), null);
    requestInfo.setRequestProto(new RequestProto(HOST, 443, true));
    TaskInfo taskInfo = HttpDownUtil
        .getTaskInfo(requestInfo, null, HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup)
        .setConnections(32)
        .setFileName(UPDATE_CORE_FILE_NAME + ".bak")
        .setFilePath(HttpDownConstant.MAIN_PATH);
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, requestInfo, null);
    AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo,
        HttpDownConstant.clientSslContext, HttpDownConstant.clientLoopGroup, null);
    bootstrap.startDown();
    return bootstrap;
  }

  private static HttpHeadsInfo buildHead() {
    return new HttpHeadsInfo() {
      {
        add("Host", HOST);
        add("Cache-Control", "max-age=0");
        add("Upgrade-Insecure-Requests", "1");
        add("User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36");
        add("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        add("Referer", "https://github.com/monkeyWie/proxyee-down/releases");
        add("Accept-Encoding", "gzip, deflate, br");
        add("Accept-Language", "zh-CN,zh;q=0.9");
      }
    };
  }

  public static void main(String[] args) throws Exception {
    GithubUpdateService githubUpdateService = new GithubUpdateService();
    UpdateInfo updateInfo = githubUpdateService.check(1.0F);
    System.out.println(updateInfo);
  }
}
