package lee.study.down.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
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

  public String buildTaskRecordBakFilePath() {
    return getFilePath() + File.separator + "." + getFileName() + ".inf.bak";
  }

  public TaskInfo buildChunkInfoList() {
    List<ChunkInfo> chunkInfoList = new ArrayList<>();
    if (getTotalSize() > 0) {  //非chunked编码
      //计算chunk列表
      for (int i = 0; i < getConnections(); i++) {
        ChunkInfo chunkInfo = new ChunkInfo();
        chunkInfo.setIndex(i);
        long chunkSize = getTotalSize() / getConnections();
        chunkInfo.setOriStartPosition(i * chunkSize);
        chunkInfo.setNowStartPosition(chunkInfo.getOriStartPosition());
        if (i == getConnections() - 1) { //最后一个连接去下载多出来的字节
          chunkSize += getTotalSize() % getConnections();
        }
        chunkInfo.setEndPosition(chunkInfo.getOriStartPosition() + chunkSize - 1);
        chunkInfo.setTotalSize(chunkSize);
        chunkInfoList.add(chunkInfo);
      }
    } else { //chunked下载
      ChunkInfo chunkInfo = new ChunkInfo();
      chunkInfo.setIndex(0);
      chunkInfo.setNowStartPosition(0);
      chunkInfo.setOriStartPosition(0);
      chunkInfoList.add(chunkInfo);
    }
    setChunkInfoList(chunkInfoList);
    return this;
  }

  public void reset() {
    startTime = lastTime = pauseTime = downSize = 0;
    chunkInfoList.forEach((chunkInfo) -> {
      chunkInfo.setStartTime(0);
      chunkInfo.setLastTime(0);
      chunkInfo.setPauseTime(0);
      chunkInfo.setDownSize(0);
      chunkInfo.setErrorCount(0);
    });
  }

  public void refresh(ChunkInfo chunkInfo) {
    lastTime = System.currentTimeMillis();
    chunkInfo.setLastTime(lastTime);
  }
}
