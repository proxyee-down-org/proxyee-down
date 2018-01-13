package lee.study.down.intercept.common;

import lee.study.proxyee.intercept.HttpProxyIntercept;

/**
 * 生成一个拦截器来处理嗅探到的下载请求
 */
public interface HttpDownInterceptFactory {

  HttpProxyIntercept create();
}
