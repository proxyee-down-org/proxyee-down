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
  private int connections;
  private long totalSize;
  private boolean supportRange;
  private long downSize;
  private long startTime = 0;
  private long lastTime = 0;
  private long pauseTime = 0;
  private int status;
  private List<ChunkInfo> chunkInfoList;

  public String buildTaskFilePath() {
    return getFilePath() + File.separator + getFileName();
  }

  public String buildTaskRecordFilePath() {
    return getFilePath() + File.separator + "." + getFileName() + ".inf";
  }

  public void reset() {
    startTime = lastTime = pauseTime = 0;
    chunkInfoList.forEach((chunkInfo) -> {
      chunkInfo.setStartTime(0);
      chunkInfo.setLastTime(0);
      chunkInfo.setPauseTime(0);
    });
  }

  public void refresh() {
    lastTime = System.currentTimeMillis();
    chunkInfoList.forEach((chunkInfo) -> {
      chunkInfo.setLastTime(lastTime);
    });
  }

  public void refresh(ChunkInfo chunkInfo) {
    lastTime = System.currentTimeMillis();
    chunkInfo.setLastTime(lastTime);
  }
}
