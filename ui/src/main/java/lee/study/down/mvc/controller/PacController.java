package lee.study.down.mvc.controller;

import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import lee.study.down.content.ContentManager;
import lee.study.down.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/res")
public class PacController {

  private final static Logger LOGGER = LoggerFactory.getLogger(PacController.class);
  private static final String pacTemple = ByteUtil
      .readContent(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("res/pd.pac"));

  @RequestMapping("/pd.pac")
  public void pac(HttpServletResponse response) {
    response.setHeader("Content-Type", "application/x-ns-proxy-autoconfig");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    try (
        OutputStream out = response.getOutputStream()
    ) {
      out.write(
          pacTemple.replace("{port}", ContentManager.CONFIG.get().getProxyPort() + "").getBytes());
    } catch (Exception e) {
      LOGGER.warn("res error:", e);
    }
  }

}
