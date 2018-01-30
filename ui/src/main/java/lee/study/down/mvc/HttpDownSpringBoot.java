package lee.study.down.mvc;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import lee.study.down.util.ConfigUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;

@SpringBootApplication
public class HttpDownSpringBoot implements InitializingBean, EmbeddedServletContainerCustomizer {

  @Value("${spring.profiles.active}")
  private String active;

  @Override
  public void afterPropertiesSet() throws Exception {
    if ("dev".equalsIgnoreCase(active.trim())) {
      ResourceLeakDetector.setLevel(Level.ADVANCED);
    }
  }

  @Override
  public void customize(ConfigurableEmbeddedServletContainer container) {
    container.setPort(Integer.parseInt(ConfigUtil.getValue("tomcat.server.port")));
  }

}
