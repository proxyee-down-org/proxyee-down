package lee.study.down.mvc.form;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.util.FileUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class NewTaskForm {

  public static final String KEY_UNZIP_FLAG = "isUnzip";
  public static final String KEY_UNZIP_PATH = "unzipPath";

  private String id;
  private String oldId;
  private String filePath;
  private String fileName;
  private int connections;
  private long totalSize;
  private boolean supportRange;
  private long downSize;
  private boolean isUnzip;
  private String unzipPath;

  public static NewTaskForm parse(HttpDownInfo httpDownInfo) {
    if (httpDownInfo != null) {
      NewTaskForm form = new NewTaskForm();
      BeanUtils.copyProperties(httpDownInfo.getTaskInfo(), form,
          new String[]{"startTime", "pauseTime", "status", "chunkInfoList"});
      boolean unzipFlag = true;
      String unzipPath =
          httpDownInfo.getTaskInfo().getFilePath() + File.separator + FileUtil
              .getFileNameNoSuffix(httpDownInfo.getTaskInfo()
                  .getFileName());
      if (httpDownInfo.getAttrs() != null) {
        if (httpDownInfo.getAttrs().get(KEY_UNZIP_FLAG) != null) {
          unzipFlag = (boolean) httpDownInfo.getAttrs().get(KEY_UNZIP_FLAG);
        }
        if (httpDownInfo.getAttrs().get(KEY_UNZIP_PATH) != null) {
          unzipPath = (String) httpDownInfo.getAttrs().get(KEY_UNZIP_PATH);
        }
      }
      form.setUnzip(unzipFlag);
      form.setUnzipPath(unzipPath);
      return form;
    }
    return null;
  }

  public static List<NewTaskForm> parse(List<HttpDownInfo> list) {
    if (list != null) {
      List<NewTaskForm> formList = new ArrayList<>();
      for (HttpDownInfo httpDownInfo : list) {
        formList.add(parse(httpDownInfo));
      }
      return formList;
    }
    return null;
  }
}
