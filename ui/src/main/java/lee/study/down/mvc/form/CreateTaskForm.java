package lee.study.down.mvc.form;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class CreateTaskForm {

  private int unzipFlag = 1;
  private String filePath;
  private String fileName;
  private String unzipPath;
  private int connections;
  private BuildTaskForm request;

  public static void main(String[] args) {
    BuildTaskForm request = new BuildTaskForm();
    request.setUrl("http://192.168.2.24/test4.zip");
    CreateTaskForm createTaskForm = new CreateTaskForm();
    createTaskForm.setFilePath("f:/down");
    createTaskForm.setRequest(request);
    System.out.println(JSON.toJSONString(createTaskForm));
  }
}
