package lee.study.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskInfo implements Serializable {
  private String id;
  private String fileName;
  private long fileSize;
  private boolean supportRange;
  private int connections;
  private String filePath;
  private long startTime;
  private long lastTime;
  private int status; //0.待下载 1.下载中 2.下载完成 3.下载失败
  private List<ChunkInfo> chunkInfoList;
}
