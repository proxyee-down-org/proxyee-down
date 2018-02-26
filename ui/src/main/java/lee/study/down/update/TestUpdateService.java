package lee.study.down.update;

import io.netty.handler.codec.http.HttpMethod;
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

public class TestUpdateService implements UpdateService {

  private static final String HOST = "192.168.2.24";

  @Override
  public UpdateInfo check(float currVersion) throws Exception {
    UpdateInfo updateInfo = new UpdateInfo();
    updateInfo.setVersion(2.1F);
    updateInfo.setUrl(
        "http://" + HOST + "/proxyee-down-core.jar");
    updateInfo.setDesc("测试更新");
    return updateInfo;
  }

  @Override
  public AbstractHttpDownBootstrap update(UpdateInfo updateInfo)
      throws Exception {
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1, HttpMethod.GET.toString(),
        updateInfo.getUrl(), buildHead(), null);
    requestInfo.setRequestProto(new RequestProto(HOST, 80, false));
    TaskInfo taskInfo = HttpDownUtil
        .getTaskInfo(requestInfo, null, null, HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup)
        .setConnections(64)
        .setFileName("proxyee-down-core.jar.bak")
        .setFilePath(HttpDownConstant.HOME_PATH);
    HttpDownInfo httpDownInfo = new HttpDownInfo(taskInfo, requestInfo, null);
    AbstractHttpDownBootstrap bootstrap = HttpDownBootstrapFactory.create(httpDownInfo, 5,
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
        add("Accept-Encoding", "gzip, deflate, br");
        add("Accept-Language", "zh-CN,zh;q=0.9");
      }
    };
  }

  public static void main(String[] args) throws Exception {
    TestUpdateService githubUpdateService = new TestUpdateService();
    UpdateInfo updateInfo = githubUpdateService.check(1.0F);
    HttpRequestInfo requestInfo = new HttpRequestInfo(HttpVer.HTTP_1_1, HttpMethod.GET.toString(),
        updateInfo.getUrl(), buildHead(), null);
    requestInfo.setRequestProto(new RequestProto("github.com", 443, true));
    System.out
        .println(HttpDownUtil.getResponse(requestInfo, null, HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup).toString());
    System.out
        .println(HttpDownUtil.getResponse(requestInfo, null, HttpDownConstant.clientSslContext,
            HttpDownConstant.clientLoopGroup).toString());
  }
}
