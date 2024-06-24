package common.commonpjt.website;

import common.commonpjt.website.entity.Website;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebScraper {

    public List<Website> scrapeWebsites(String keyword) {
        List<Website> websites = new ArrayList<>();
        String searchUrl = "https://www.google.com/search?q=" + keyword;

        try {
            Document doc = Jsoup.connect(searchUrl).get();
            Elements results = doc.select("div.g");

            for (Element result : results) {
                Element link = result.selectFirst("a");
                Element title = result.selectFirst("h3");
                Element description = result.selectFirst("span.aCOpRe");

                if (link != null && title != null && description != null) {
                    String url = link.attr("href");
                    String siteTitle = title.text();
                    String siteDescription = description.text();

                    websites.add(new Website(url, siteTitle, siteDescription));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return websites;
    }
}