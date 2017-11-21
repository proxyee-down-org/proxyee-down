package lee.study.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lee.study.HttpDownServer;
import lee.study.down.HttpDown;
import lee.study.down.HttpDownCallback;
import lee.study.form.DownForm;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class DownController {

  @RequestMapping("/getTask")
  @ResponseBody
  public HttpDown.DownInfo getTask(@RequestParam int id) {
    return id < HttpDownServer.downContent.size() ? HttpDownServer.downContent.get(id).getDownInfo()
        : null;
  }

  @RequestMapping("/startTask")
  @ResponseBody
  public String startTask(@RequestBody DownForm downForm) {
    try {
      HttpDown.fastDown(HttpDownServer.downContent.get(downForm.getId()), downForm.getConnections(), HttpDownServer.loopGroup,
          downForm.getPath(), new HttpDownCallback() {
            @Override
            public void start() {

            }

            @Override
            public void progress(int index, long chunkDownSize, long chunkTotalSize,
                long fileDownSize,
                long fileTotalSize) {
              System.out.println("总大小:"+fileTotalSize+"\t已下载："+fileDownSize);
//              System.out.println("文件块("+index+")总大小:"+chunkTotalSize+"\t已下载："+chunkDownSize);
            }

            @Override
            public void error(int index) {

            }

            @Override
            public void done(int index) {
              System.out.println("index-"+index+"下载完成");
            }
          });
    } catch (Exception e) {
      return "N";
    }
    return "Y";
  }

  @RequestMapping("/test")
  public String test(HttpServletResponse response) {
    try {
      response.setContentType("text/html");
      response.setContentLength(4);
      OutputStream outputStream = response.getOutputStream();
      outputStream.write('a');
      outputStream.write('a');
      outputStream.flush();
      outputStream.write('a');
      outputStream.write('a');
      Thread.sleep(5000);
      outputStream.write('b');
      outputStream.write('b');
      outputStream.write('b');
      outputStream.write('b');
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
