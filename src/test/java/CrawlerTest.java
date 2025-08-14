import org.junit.jupiter.api.Test;
import org.mamallc.crawler.Crawler;
import org.mamallc.utils.URLString;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CrawlerTest {
    Map<String, String> urls = new HashMap<>();
    String rootURL = "https://www.wikipedia.org";
    Crawler crawler = new Crawler();

    @Test
    public void processURLTest() {
        urls.put("https://www.wikipedia.org", "https://www.wikipedia.org");
        urls.put("/wiki", "https://www.wikipedia.org/wiki");
        urls.put("//en.wikipedia.org", "https://en.wikipedia.org");
        urls.put("#", "https://www.wikipedia.org#");

        for (Map.Entry<String, String> entry : urls.entrySet()) {
            assertEquals(entry.getValue(), URLString.processURL(entry.getKey(), entry.getValue(), rootURL));
        }
    }

    @Test
    public void crawlTest() {
        String url = "https://owasp.org/chapters/";
        crawler.crawlPage(url, false, true);
    }
}
