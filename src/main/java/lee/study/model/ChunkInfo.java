package lee.study.model;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkInfo {
  private final static AtomicInteger seq = new AtomicInteger();

  private int id;
  private long downSize;
  private long totalSize;
  private long startTime;
  private long lastTime;
  private int status; //1.下载中 2.下载完成 3.下载失败

  public ChunkInfo(long downSize, long totalSize, long startTime, long lastTime, int status) {
    this.id = seq.getAndIncrement();
    this.downSize = downSize;
    this.totalSize = totalSize;
    this.startTime = startTime;
    this.lastTime = lastTime;
    this.status = status;
  }
}
