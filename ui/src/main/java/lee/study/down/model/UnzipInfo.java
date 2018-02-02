package lee.study.down.model;

import lee.study.down.io.BdyZip.BdyZipEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UnzipInfo {

  private String id;
  private String type;
  private BdyZipEntry entry;
  private long totalFileSize;
  private long totalWriteSize;
  private long currFileSize;
  private long currWriteSize;
  private long startTime;
  private long endTime;
}
