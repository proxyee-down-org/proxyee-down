package lee.study.down.update;

import io.netty.handler.codec.http.HttpMethod;
import lee.study.down.HttpDownBootstrap;
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

public class GithubUpdateService implements UpdateService {

  private static final String UPDATE_CORE_FILE_NAME = "proxyee-down-core.jar";
  private static final String HOST = "github.com";

  @Override
  public UpdateInfo check(float currVersion) throws Exception {
    UpdateInfo updateInfo = new UpdateInfo();
    Document document = Jsoup.connect("https://github.com/monkeyWie/proxyee-down/releases").get();
    Element releaseDiv = document.select(".release-body.commit").get(0);
    Element version = releaseDiv.select(".release-title.text-normal").get(0);
    float maxVersion = Float.parseFloat(version.text());
    if (maxVersion > currVersion) {
      updateInfo.setVersion(maxVersion);
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
  public HttpDownBootstrap update(UpdateInfo updateInfo)
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
    HttpDownBootstrap bootstrap = new HttpDownBootstrap(httpDownInfo,
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
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1, HttpMethod.GET.toString(),
        updateInfo.getUrl(), buildHead(), null);
    requestInfo.setRequestProto(new RequestProto("github.com", 443, true));
    System.out.println(HttpDownUtil.getResponse(requestInfo, HttpDownConstant.clientSslContext,
        HttpDownConstant.clientLoopGroup).toString());
    System.out.println(HttpDownUtil.getResponse(requestInfo, HttpDownConstant.clientSslContext,
        HttpDownConstant.clientLoopGroup).toString());
  }
}
