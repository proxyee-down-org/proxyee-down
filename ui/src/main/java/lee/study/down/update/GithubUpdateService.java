package lee.study.down.update;

import io.netty.handler.codec.http.HttpMethod;
import lee.study.down.HttpDownBootstrap;
import lee.study.down.constant.HttpDownConstant;
import lee.study.down.content.ContentManager;
import lee.study.down.dispatch.HttpDownCallback;
import lee.study.down.model.ChunkInfo;
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
      updateInfo.setUrl(
          "https://" + HOST + releaseDiv.select(".d-block.py-2").get(1).select("a").attr("href"));
      updateInfo.setDesc(releaseDiv.select(".markdown-body").html());
      System.out.println(updateInfo.toString());
      return updateInfo;
    }
    return null;
  }

  @Override
  public HttpDownBootstrap update(UpdateInfo updateInfo, HttpDownCallback callback)
      throws Exception {
    HttpHeadsInfo headsInfo = new HttpHeadsInfo();
    headsInfo.add("Host", HOST);
    headsInfo.add("Cache-Control", "max-age=0");
    headsInfo.add("Upgrade-Insecure-Requests", "1");
    headsInfo.add("User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36");
    headsInfo.add("Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    headsInfo.add("Referer", "https://github.com/monkeyWie/proxyee-down/releases");
    headsInfo.add("Accept-Encoding", "gzip, deflate, br");
    headsInfo.add("Accept-Language", "zh-CN,zh;q=0.9");
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1, HttpMethod.GET.toString(),
        updateInfo.getUrl(), headsInfo, null);
    requestInfo.setRequestProto(new RequestProto("github.com", 443, true));
    TaskInfo taskInfo = HttpDownUtil
        .getTaskInfo(requestInfo, null, HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup)
        .setConnections(16)
        .setFileName("proxyee-down-core.jar")
        .setFilePath(HttpDownConstant.LIB_PATH)
        .buildChunkInfoList();
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, requestInfo, null);
    return new HttpDownBootstrap(httpDownInfo,
        HttpDownConstant.clientSslContext, HttpDownConstant.clientLoopGroup, callback);
  }

  public static void main(String[] args) throws Exception {
    GithubUpdateService githubUpdateService = new GithubUpdateService();
    UpdateInfo updateInfo = githubUpdateService.check(1.0F);
    githubUpdateService.update(updateInfo, new HttpDownCallback() {
      @Override
      public void onStart(HttpDownInfo httpDownInfo) throws Exception {

      }

      @Override
      public void onChunkStart(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {

      }

      @Override
      public void onProgress(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {
        System.out.println(
            "下载中:" + httpDownInfo.getTaskInfo().getDownSize() + "/" + httpDownInfo.getTaskInfo()
                .getTotalSize());
      }

      @Override
      public void onPause(HttpDownInfo httpDownInfo) throws Exception {

      }

      @Override
      public void onContinue(HttpDownInfo httpDownInfo) throws Exception {

      }

      @Override
      public void onError(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo, Throwable cause)
          throws Exception {

      }

      @Override
      public void onChunkDone(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {

      }

      @Override
      public void onDone(HttpDownInfo httpDownInfo) throws Exception {

      }
    });
  }
}
