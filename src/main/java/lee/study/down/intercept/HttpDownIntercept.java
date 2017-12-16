package lee.study.down.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lee.study.down.HttpDownServer;
import lee.study.down.model.TaskInfo;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.down.util.HttpDownUtil;

public class HttpDownIntercept extends HttpProxyIntercept {

  @Override
  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    TaskInfo taskInfo = HttpDownUtil
        .getTaskInfo(pipeline.getHttpRequest(), httpResponse.headers(), HttpDownServer.LOOP_GROUP);
    HttpDownUtil.startDownTask(taskInfo, pipeline.getHttpRequest(), httpResponse, clientChannel);
  }
}
