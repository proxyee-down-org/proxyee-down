package org.pdown.gui.extension.mitm.util;

import com.github.monkeywie.proxyee.crt.CertUtil;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.util.ExecUtil;
import sun.security.x509.X500Name;

/**
 * 用于处理系统的证书安装、查询和卸载
 */
public class ExtensionCertUtil {


  /**
   * 在指定目录生成一个ca证书和私钥
   */
  public static void buildCert(String path, String subjectName) throws Exception {
    //生成ca证书和私钥
    KeyPair keyPair = CertUtil.genKeyPair();
    File priKeyFile = FileUtil.createFile(path + File.separator + ".ca_pri.der", true);
    File caCertFile = FileUtil.createFile(path + File.separator + "ca.crt", false);
    Files.write(Paths.get(priKeyFile.toURI()), keyPair.getPrivate().getEncoded());
    Files.write(Paths.get(caCertFile.toURI()),
        CertUtil.genCACert(
            "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + subjectName,
            new Date(),
            new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3650)),
            keyPair)
            .getEncoded());
  }

  /**
   * 安装证书
   */
  public static void installCert(File file) throws IOException {
    String path = file.getPath();
    if (OsUtil.isWindows()) {
      ExecUtil.execBlock("certutil",
          "-addstore",
          "-user",
          "root",
          path);
    } else if (OsUtil.isMac()) {
      ExecUtil.httpGet("http://127.0.0.1:" + DownApplication.macToolPort + "/cert/install?path=" + URLEncoder.encode(path, "utf-8"));
    }
  }

  /**
   * 通过证书subjectName和sha1，判断系统是否已安装该证书
   */
  public static boolean isInstalledCert(File file) throws Exception {
    if (!file.exists()) {
      return false;
    }
    if (OsUtil.isUnix()) {
      return true;
    }
    X509Certificate cert = CertUtil.loadCert(file.toURI());
    String subjectName = ((X500Name) cert.getSubjectDN()).getCommonName();
    String sha1 = getCertSHA1(cert);
    return findCertList(subjectName).toUpperCase().replaceAll("\\s", "").indexOf(":" + sha1.toUpperCase()) != -1;
  }

  /**
   * 通过证书subjectName，判断系统是否已安装此subjectName的证书
   */
  public static boolean existsCert(String subjectName) throws IOException {
    if (OsUtil.isWindows() && findCertList(subjectName).toUpperCase().indexOf("=====") != -1) {
      return true;
    } else if (OsUtil.isMac() && findCertList(subjectName).toUpperCase().indexOf("BEGIN CERTIFICATE") != -1) {
      return true;
    }
    return false;
  }

  /**
   * 通过证书name，卸载证书
   */
  public static void uninstallCert(String subjectName) throws IOException {
    if (OsUtil.isWindows()) {
      Pattern pattern = Pattern.compile("(?i)\\(sha1\\):\\s(.*)\r?\n");
      String certList = findCertList(subjectName);
      Matcher matcher = pattern.matcher(certList);
      while (matcher.find()) {
        String hash = matcher.group(1).replaceAll("\\s", "");
        ExecUtil.execBlock("certutil",
            "-delstore",
            "-user",
            "root",
            hash);
      }
    } else if (OsUtil.isMac()) {
      String certList = findCertList(subjectName);
      Pattern pattern = Pattern.compile("(?i)SHA-1 hash:\\s(.*)\r?\n");
      Matcher matcher = pattern.matcher(certList);
      while (matcher.find()) {
        String hash = matcher.group(1);
        ExecUtil.httpGet("http://127.0.0.1:" + DownApplication.macToolPort + "/cert/uninstall"
            + "?hash=" + hash);
      }
    }
  }

  //查询证书列表
  private static String findCertList(String subjectName) throws IOException {
    if (OsUtil.isWindows()) {
      return ExecUtil.exec("certutil ",
          "-store",
          "-user",
          "root",
          subjectName);
    } else if (OsUtil.isMac()) {
      return ExecUtil.exec("security",
          "find-certificate",
          "-a",
          "-c",
          subjectName,
          "-p",
          "-Z",
          "/Library/Keychains/System.keychain");
    }
    return null;
  }

  private static String getCertSHA1(X509Certificate certificate) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] der = certificate.getEncoded();
    md.update(der);
    return btsToHex(md.digest());
  }

  private static String btsToHex(byte[] bts) {
    StringBuilder str = new StringBuilder();
    for (byte b : bts) {
      str.append(String.format("%2s", Integer.toHexString(b & 0xFF)).replace(" ", "0"));
    }
    return str.toString();
  }

  public static void main(String[] args) throws Exception {
    String subjectName = "ProxyeeDown CA";
    String path = "f:/test/";
    File certFile = new File(path + "ca.crt");
    //证书还未安装
    if (!isInstalledCert(certFile)) {
      if (existsCert(subjectName)) {
        //存在无用证书需要卸载
        uninstallCert(subjectName);
      }
      //生成新的证书
      buildCert(path, subjectName);
      //安装
      installCert(certFile);
    }
  }
}
