package lee.study.down.constant;

public class HttpDownStatus {

  /**
   * 等待下载
   */
  public final static int WAIT = 0;
  /**
   * 正常发起的连接
   */
  public final static int CONNECTING_NORMAL = 1;
  /**
   * 失败后发起的连接
   */
  public final static int CONNECTING_FAIL = 2;
  /**
   * 继续下载发起的链接
   */
  public final static int CONNECTING_CONTINUE = 3;
  /**
   * 下载中
   */
  public final static int RUNNING = 4;
  /**
   * 暂停下载
   */
  public final static int PAUSE = 5;
  /**
   * 下载失败
   */
  public final static int FAIL = 6;
  /**
   * 下载完成
   */
  public final static int DONE = 7;
  /**
   * 响应错误等待连接
   */
  public final static int ERROR_WAIT_CONNECT = 8;
}
