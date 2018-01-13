package lee.study.down.update;

import lee.study.down.model.HttpDownInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GithubUpdateCheck implements UpdateCheck {

  @Override
  public HttpDownInfo check() throws Exception {
    Document document = Jsoup.connect("https://github.com/monkeyWie/proxyee-down/releases").get();
    Elements elements = document.select(".release-title.text-normal");
    elements.forEach((e) -> System.out.println(e.text()));
    return null;
  }

  public static void main(String[] args) throws Exception {
    new GithubUpdateCheck().check();
  }
}
