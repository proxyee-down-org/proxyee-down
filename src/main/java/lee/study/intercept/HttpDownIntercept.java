package lee.study.intercept;

import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import lee.study.down.HttpDown;
import lee.study.proxyee.NettyHttpProxyServer;
import lee.study.proxyee.intercept.HttpProxyIntercept;

public class HttpDownIntercept extends HttpProxyIntercept {
    public static NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);

    private HttpRequest httpRequest;
    private boolean downFlag = false;

    @Override
    public boolean beforeRequest(Channel channel, HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return true;
    }

    @Override
    public boolean afterResponse(Channel clientChannel, Channel proxyChannel, final HttpResponse httpResponse) {
        downFlag = false;
        HttpHeaders httpHeaders = httpResponse.headers();
        String disposition = httpHeaders.get(HttpHeaderNames.CONTENT_DISPOSITION);
        if (disposition != null) {  //先根据CONTENT_DISPOSITION:ATTACHMENT来判断是否下载请求
            //没有Range请求头(audio标签发起的)并且不是ajax请求(没有X-Requested-With请求头)
            if (disposition.contains(HttpHeaderValues.ATTACHMENT) && !httpRequest.headers().contains(HttpHeaderNames.RANGE) && !httpRequest.headers().contains("x-requested-with")) {
                downFlag = true;
            }
        }
        if (!downFlag) {  //再根据URL和CONTENT_TYPE来判断是否下载请求
            if (httpRequest.uri().matches("^.*\\.[^.]+$")) { //url后缀为.xxx
                String contentType = httpHeaders.get(HttpHeaderNames.CONTENT_TYPE);
                if (contentType != null
                        && contentType.contains("application/")
                        && !contentType.contains("javascript")
                        && !contentType.contains("json")
                        && !contentType.contains("shockwave-flash")) {
                    downFlag = true;
                }
            }
        }
        if (downFlag) {   //如果是下载，返回给浏览器一个empty response
//            System.out.println(httpResponse.toString());
            final HttpHeaders resHeaders = new DefaultHttpHeaders();
            for (String key : httpResponse.headers().names()) {
                resHeaders.set(key, httpHeaders.get(key));
            }
            new Thread(() -> HttpDown.download(httpRequest, resHeaders, loopGroup)).start();
            httpHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING);
            httpHeaders.remove(HttpHeaderNames.CONTENT_DISPOSITION);
            httpResponse.setStatus(HttpResponseStatus.OK);
            httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
            httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, 0);
            clientChannel.writeAndFlush(httpResponse);
            clientChannel.writeAndFlush(new DefaultLastHttpContent());
            clientChannel.close();
            return false;
        }
        return true;
    }

    @Override
    public boolean afterResponse(Channel channel, Channel proxyChannel, HttpContent httpContent) {
        if (downFlag) { //如果是下载丢弃真实服务器数据
            return false;
        }
        return true;
    }
}
