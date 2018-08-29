package org.pdown.gui.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import org.pdown.gui.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckUpdate {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckUpdate.class);

  public static VersionInfo doCheck() {
    String adminServer = ConfigUtil.getString("adminServer");
    double currVersion = Double.parseDouble(ConfigUtil.getString("version"));
    try {
      URL url = new URL(adminServer + "checkUpdate");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      if (connection.getResponseCode() == 200) {
        ObjectMapper objectMapper = new ObjectMapper();
        VersionInfo versionInfo = objectMapper.readValue(connection.getInputStream(), VersionInfo.class);
        if (versionInfo.getVersion() > currVersion) {
          return versionInfo;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Check update error", e);
    }
    return null;
  }
}
