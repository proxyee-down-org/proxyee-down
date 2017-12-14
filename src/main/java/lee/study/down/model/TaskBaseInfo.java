package lee.study.down.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskBaseInfo implements Serializable {

  private static final long serialVersionUID = 2097496693195193361L;
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
}
