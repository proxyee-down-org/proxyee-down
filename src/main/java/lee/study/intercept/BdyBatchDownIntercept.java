package lee.study.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import java.util.UUID;
import lee.study.down.HttpDown;
import lee.study.model.TaskInfo;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.util.HttpDownUtil;

public class BdyBatchDownIntercept extends HttpProxyIntercept {

  //16M
  private final static double CHUNK_16M = 1024 * 1024 * 16;

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel,
      HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
    if (HttpDownUtil.checkReferer(httpRequest, "^https?://pan.baidu.com/disk/home.*$")
        && HttpDownUtil.checkUrl(httpRequest, "^.*method=batchdownload.*$")) {
      HttpHeaders resHeaders = httpResponse.headers();
      long fileSize = HttpDown.getDownFileSize(resHeaders);
      //百度合并下载分段最多为16M
      int connections = (int) Math.ceil(fileSize / CHUNK_16M);
      TaskInfo taskInfo = new TaskInfo(
          UUID.randomUUID().toString(), HttpDown.getDownFileName(httpRequest, resHeaders), fileSize,
          true, connections, "", 0, 0, 0, null);
      HttpDownUtil.startDownTask(taskInfo, httpRequest, httpResponse, clientChannel);
      return;
    }
    pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
  }
}
