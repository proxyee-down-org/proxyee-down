package lee.study.down.util;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lee.study.down.HttpDownServer;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WsUtil {

  public static void sendMsg() {
    try {
      List<TaskInfo> taskInfos = new LinkedList<>();
      for (Entry<String, HttpDownInfo> downInfoEntry : HttpDownServer.DOWN_CONTENT.entrySet()) {
        HttpDownInfo httpDownInfo = downInfoEntry.getValue();
        if (httpDownInfo.getTaskInfo().getStatus() != 0) {
          taskInfos.add(httpDownInfo.getTaskInfo());
        }
      }
      if (taskInfos.size() > 0) {
        TextMessage message = new TextMessage(JSON.toJSONString(taskInfos));
        for (Entry<String, WebSocketSession> entry : HttpDownServer.WS_CONTENT.entrySet()) {
          WebSocketSession session = entry.getValue();
          if (session.isOpen()) {
            synchronized (session) {
              session.sendMessage(message);
            }
          }
        }
      }
    } catch (Exception e) {
      HttpDownServer.LOGGER.warn("sendMsg",e);
    }
  }
}
