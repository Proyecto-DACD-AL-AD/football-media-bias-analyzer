package org.ulpgc.dacd.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ulpgc.dacd.model.NewsArticle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AsRssScraper implements NewsScraper {

    private static final String BASE_URL = "https://feeds.as.com/mrss-s/list/as/site/as.com/tag/%s_a/";
    private static final String SOURCE_NAME = "AS";

    private static final Map<String, String> TEAM_URL_NAMES = Map.of(
            "Real Madrid CF", "real_madrid",
            "FC Barcelona", "barcelona",
            "Real Betis Balompié", "real_betis",
            "Sevilla FC", "sevilla_futbol_club",
            "Real Sociedad de Fútbol", "real_sociedad",
            "Athletic Club", "athletic",
            "UD Las Palmas", "las_palmas",
            "CD Tenerife", "cd_tenerife"
    );

    @Override
    public List<NewsArticle> scrape(String teamName) {
        List<NewsArticle> articles = new ArrayList<>();
        String teamSlug = TEAM_URL_NAMES.get(teamName);

        if (teamSlug == null) return articles;

        String url = String.format(BASE_URL, teamSlug);

        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("item");

            for (Element item : items) {
                articles.add(parseArticle(item, teamName));
            }
        } catch (IOException e) {
            System.err.println("Error al conectar con el RSS de AS para " + teamName + ": " + e.getMessage());
        }

        return articles;
    }

    private NewsArticle parseArticle(Element item, String teamName) {
        String title = extractText(item, "title");
        String link = extractText(item, "link");
        String date = extractText(item, "pubDate");

        return new NewsArticle(title, link, date, SOURCE_NAME, teamName);
    }

    private String extractText(Element item, String tag) {
        Element element = item.selectFirst(tag);
        return element != null ? element.text() : "";
    }
}