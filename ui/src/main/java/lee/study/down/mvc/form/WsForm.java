package lee.study.down.mvc.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class WsForm {

  private int type;
  private Object data;
}
