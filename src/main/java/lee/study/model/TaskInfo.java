package lee.study.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskInfo {
  private int id;
  private String fileName;
  private long fileSize;
  private boolean supportRange;
  private int connections;
  private String filePath;
  private long startTime;
  private long lastTime;
  private int status; //0.待下载 1.下载中 2.下载完成 3.下载失败
  private List<ChunkInfo> chunkInfoList;

  public TaskInfo(String fileName, long fileSize, boolean supportRange, int connections,
      String filePath, long startTime, long lastTime, int status,
      List<ChunkInfo> chunkInfoList) {
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.supportRange = supportRange;
    this.connections = connections;
    this.filePath = filePath;
    this.startTime = startTime;
    this.lastTime = lastTime;
    this.status = status;
    this.chunkInfoList = chunkInfoList;
  }
}
