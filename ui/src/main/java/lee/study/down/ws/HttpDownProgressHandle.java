package lee.study.down.ws;

import lee.study.down.content.HttpWsContent;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class HttpDownProgressHandle extends TextWebSocketHandler {

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    HttpWsContent.put(session.getId(), session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    HttpWsContent.remove(session.getId());
  }
}
