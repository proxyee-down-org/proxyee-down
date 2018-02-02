package lee.study.down.content;

import java.util.ArrayList;
import java.util.List;
import lee.study.down.model.UnzipInfo;
import lee.study.down.mvc.form.WsForm;
import lee.study.down.mvc.ws.WsDataType;

public class UnzipContent {

  private static List<UnzipInfo> unzipContent;

  public WsForm buildWsForm() {
    return null;
  }

  public void init() {
    unzipContent = new ArrayList<>();
  }
}
