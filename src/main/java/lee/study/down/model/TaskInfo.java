package lee.study.down.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import lee.study.down.dispatch.HttpDownCallback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TaskInfo extends TaskBaseInfo implements Serializable {

  private static final long serialVersionUID = 4813413517396555930L;
  private List<ChunkInfo> chunkInfoList;

  public TaskInfo(String id, String filePath, String fileName, int connections, long totalSize,
      boolean supportRange, long downSize, long startTime, long lastTime, int status) {
    super(id, filePath, fileName, connections, totalSize, supportRange, downSize, startTime,
        lastTime,
        status);
  }

  @JsonIgnore
  private transient HttpDownCallback callback;
}
