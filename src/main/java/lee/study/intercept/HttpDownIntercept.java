package lee.study.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import lee.study.HttpDownServer;
import lee.study.down.HttpDown;
import lee.study.model.TaskInfo;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.util.HttpDownUtil;

public class HttpDownIntercept extends HttpProxyIntercept {

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel,
      HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
    TaskInfo taskInfo = HttpDown
        .getTaskInfo(httpRequest, httpResponse.headers(), HttpDownServer.LOOP_GROUP);
    HttpDownUtil.startDownTask(taskInfo, httpRequest, httpResponse, clientChannel);
    return;
  }
}
