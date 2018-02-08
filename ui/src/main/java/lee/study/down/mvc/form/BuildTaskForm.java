package lee.study.down.mvc.form;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class BuildTaskForm {

  private String url;
  private List<Map<String, String>> heads;
  private String body;
}
