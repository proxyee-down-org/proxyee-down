package lee.study.down.mvc.form;

import lee.study.down.model.TaskInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskInfoForm extends TaskInfo {

  private String url;
}
