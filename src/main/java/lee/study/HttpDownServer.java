package lee.study;

import lee.study.intercept.HttpDownIntercept;
import lee.study.proxyee.NettyHttpProxyServer;

public class HttpDownServer {
    private void start(int port){
        new NettyHttpProxyServer().initProxyInterceptFactory(() -> new HttpDownIntercept()).start(port);
    }

    public static void main(String[] args) {
        new HttpDownServer().start(9999);
    }
}
