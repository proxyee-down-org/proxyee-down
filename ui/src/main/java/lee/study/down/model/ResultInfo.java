package lee.study.down.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResultInfo {

  private int status = ResultStatus.SUCC.getCode();
  private Object data;
  private String msg = MSG_SUCC;

  public static final String MSG_SUCC = "操作成功";
  public static final String MSG_ERROR = "服务器异常";

  public enum ResultStatus {
    SUCC(200), BAD(400), ERROR(500);

    private int code;

    ResultStatus(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int code) {
      this.code = code;
    }
  }
}
