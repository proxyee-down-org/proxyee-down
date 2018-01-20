package lee.study.down.exception;

import com.alibaba.fastjson.JSON;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lee.study.down.HttpDownServer;
import lee.study.down.model.ResultInfo;
import lee.study.down.model.ResultInfo.ResultStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Component
public class RestExceptionHandler implements HandlerExceptionResolver {

  @Override
  public ModelAndView resolveException(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, Exception e) {
    HttpDownServer.LOGGER.error("rest error:",e);
    ModelAndView modelAndView = new ModelAndView();
    try {
      ResultInfo resultInfo = new ResultInfo().setStatus(ResultStatus.ERROR.getCode())
          .setMsg(ResultInfo.MSG_ERROR);
      Map<String, Object> attr = JSON.parseObject(JSON.toJSONString(resultInfo), Map.class);
      MappingJackson2JsonView view = new MappingJackson2JsonView();
      view.setAttributesMap(attr);
      modelAndView.setView(view);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    return modelAndView;
  }
}
