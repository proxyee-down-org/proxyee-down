package lee.study;

import io.netty.channel.socket.nio.NioSocketChannel;

public class MyNioSocketChannel extends NioSocketChannel {

  @Override
  protected void doFinishConnect() throws Exception {
    boolean flag = false;
    try {
      flag = !javaChannel().finishConnect();
    } catch (Exception e) {
      System.out.println("catch exception!!!!");
      javaChannel().close();
      //e.printStackTrace();
    }
    if (flag) {
      throw new Error();
    }
  }
}
