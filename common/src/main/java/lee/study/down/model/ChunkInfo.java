package lee.study.down.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkInfo implements Serializable {

  private static final long serialVersionUID = 231649750985696846L;
  private int index;
  private long oriStartPosition; //原本的起始字节位
  private long nowStartPosition; //重新计算后的起始字节位
  private long endPosition; //结束字节位
  private long downSize = 0;
  private long totalSize;
  private long startTime = 0;
  private long lastTime = 0;
  private long pauseTime = 0;
  private int status = 0;
  private int errorCount = 0;
  private long lastDownTime = 0;
}
