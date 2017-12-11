package lee.study.down.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecordInfo implements Serializable {

  private static final long serialVersionUID = -4192581417524651208L;
  private String taskId;
  private String filePath;
  private String fileName;
}
