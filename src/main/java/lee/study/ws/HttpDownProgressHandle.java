package lee.study.ws;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.HttpDownServer;
import lee.study.model.HttpDownInfo;
import lee.study.model.TaskInfo;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class HttpDownProgressHandle extends TextWebSocketHandler {

  private static Thread progressThread = new Thread(() -> {
    try {
      while (true) {
        if(HttpDownServer.downContent!=null&&HttpDownServer.downContent.size()>0){
          for (Entry<Integer, HttpDownInfo> entry : HttpDownServer.downContent.entrySet()) {
            HttpDownInfo httpDownModel = entry.getValue();
            if (httpDownModel.getTaskInfo().getStatus() == 1) {
              sendMsg("progress",httpDownModel.getTaskInfo());
            }
          }
          TimeUnit.MILLISECONDS.sleep(300);
        }
      }
    }  catch (Exception e){
      e.printStackTrace();
    }
  });

  public HttpDownProgressHandle() {
    super();
    progressThread.start();
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    HttpDownServer.wsContent.put(session.getId(), session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//    System.out.println(message.toString());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    HttpDownServer.wsContent.remove(session.getId());
  }

  public static void sendMsg(String type, TaskInfo taskInfo) {
    try {
      for (Entry<String, WebSocketSession> entry : HttpDownServer.wsContent.entrySet()) {
        WebSocketSession session = entry.getValue();
        if (session.isOpen()) {
          Map<String, Object> msg = new HashMap<>();
          msg.put("type", type);
          msg.put("taskInfo", taskInfo);
          TextMessage message = new TextMessage(JSON.toJSONString(msg));
          synchronized (session){
            session.sendMessage(message);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
