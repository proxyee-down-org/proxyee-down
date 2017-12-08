package lee.study.down.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import lee.study.down.dispatch.HttpDownCallback;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskInfo implements Serializable {

  private static final long serialVersionUID = 4813413517396555930L;
  private String id;
  private String filePath;
  private String fileName;
  private int connections;
  private long totalSize;
  private boolean supportRange;
  private long downSize;
  private long startTime;
  private long lastTime;
  private int status; //0.待下载 1.下载中 2.下载完成 3.下载失败
  private List<ChunkInfo> chunkInfoList;

  @JsonIgnore
  private transient HttpDownCallback callback;
}
