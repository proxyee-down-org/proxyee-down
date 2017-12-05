package lee.study.ws;

import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lee.study.HttpDownServer;
import lee.study.model.ChunkInfo;
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
        if (HttpDownServer.DOWN_CONTENT != null && HttpDownServer.DOWN_CONTENT.size() > 0) {
          for (Entry<String, HttpDownInfo> entry : HttpDownServer.DOWN_CONTENT.entrySet()) {
            TaskInfo taskInfo = entry.getValue().getTaskInfo();
            if (taskInfo.getStatus() == 1) {
              taskInfo.setLastTime(System.currentTimeMillis());
              for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                chunkInfo.setLastTime(System.currentTimeMillis());
              }
            }
            sendMsg("progress", taskInfo);
          }
          TimeUnit.MILLISECONDS.sleep(200);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  });

  public HttpDownProgressHandle() {
    super();
    progressThread.start();
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    HttpDownServer.WS_CONTENT.put(session.getId(), session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//    System.out.println(message.toString());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    HttpDownServer.WS_CONTENT.remove(session.getId());
  }

  public static void sendMsg(String type, TaskInfo taskInfo) {
    try {
      for (Entry<String, WebSocketSession> entry : HttpDownServer.WS_CONTENT.entrySet()) {
        WebSocketSession session = entry.getValue();
        if (session.isOpen()) {
          Map<String, Object> msg = new HashMap<>();
          msg.put("type", type);
          msg.put("taskInfo", taskInfo);
          TextMessage message = new TextMessage(JSON.toJSONString(msg));
          synchronized (session) {
            session.sendMessage(message);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
