package lee.study.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkInfo implements Serializable {
  private String id;
  private long downSize;
  private long totalSize;
  private long startTime;
  private long lastTime;
  private int status; //1.下载中 2.下载完成 3.下载失败
}
