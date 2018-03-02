package lee.study.down.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInfo {

  private String url;
  private float version;
  private String versionStr;
  private String desc;
}
