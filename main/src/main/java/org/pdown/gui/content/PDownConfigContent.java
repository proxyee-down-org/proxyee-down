package org.pdown.gui.content;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.pdown.gui.entity.PDownConfigInfo;
import org.pdown.rest.base.content.PersistenceContent;
import org.pdown.rest.util.PathUtil;

public class PDownConfigContent extends PersistenceContent<PDownConfigInfo, PDownConfigContent> {

  private static final PDownConfigContent INSTANCE = new PDownConfigContent();

  public static PDownConfigContent getInstance() {
    return INSTANCE;
  }

  @Override
  protected TypeReference type() {
    return new TypeReference<PDownConfigInfo>() {
    };
  }

  @Override
  protected String savePath() {
    return PathUtil.ROOT_PATH + File.separator + "pdown.cfg";
  }

  @Override
  protected PDownConfigInfo defaultValue() {
    PDownConfigInfo pDownConfigInfo = new PDownConfigInfo();
    //取系统默认语言
    Locale defaultLocale = Locale.getDefault();
    pDownConfigInfo.setLocale(defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
    //插件文件服务器
    List<String> extFileServers = new ArrayList<>();
    extFileServers.add("https://github.com/proxyee-down-org/proxyee-down-extension/raw/master");
    extFileServers.add("http://pdown.org/extensions");
    pDownConfigInfo.setExtFileServers(extFileServers);
    return pDownConfigInfo;
  }
}
