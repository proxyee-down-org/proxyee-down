package lee.study.down.model;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TaskInfo implements Serializable {

  private static final long serialVersionUID = 4813413517396555930L;
  private String id;
  private String filePath;
  private String fileName;
  private int connections = 1;
  private long totalSize;
  private boolean supportRange;
  private long downSize;
  private long startTime;
  private long lastTime;
  private long pauseTime = 0;
  private int status; //0.待下载 1.下载中 2.下载完成 3.下载失败 4.下载暂停
  private List<ChunkInfo> chunkInfoList;

  public String buildTaskFilePath() {
    return getFilePath() + File.separator + getFileName();
  }
}
