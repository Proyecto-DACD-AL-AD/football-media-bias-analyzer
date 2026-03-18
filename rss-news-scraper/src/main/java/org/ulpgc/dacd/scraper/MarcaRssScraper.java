package org.ulpgc.dacd.scraper;

import org.ulpgc.dacd.model.NewsArticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class MarcaRssScraper implements NewsScraper {

    private static final String BASE_URL = "https://objetos.estaticos-marca.com/rss/futbol/%s.xml";
    private static final String SOURCE_NAME = "Marca";

    private static final Map<String, String> TEAM_URL_NAMES = Map.of(
            "Real Madrid CF", "real-madrid",
            "FC Barcelona", "barcelona",
            "Real Betis Balompié", "betis",
            "Sevilla FC", "sevilla",
            "Real Sociedad de Fútbol", "real-sociedad",
            "Athletic Club", "athletic",
            "UD Las Palmas", "las-palmas"
    );

    @Override
    public List<NewsArticle> scrape(String teamName) {
        List<NewsArticle> articles = new ArrayList<>();
        String url = String.format(BASE_URL, formatTeamName(teamName));

        if (teamName.equalsIgnoreCase("CD Tenerife")) {
            System.out.println("Aviso: Marca no provee RSS para " + teamName);
            return articles;
        }

        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("item");

            for (Element item : items) {
                articles.add(parseArticle(item, teamName));
            }
        } catch (IOException e) {
            System.err.println("Error al conectar con el RSS de Marca para " + teamName + ": " + e.getMessage());
        }

        return articles;
    }

    private String formatTeamName(String teamName) {
        return TEAM_URL_NAMES.getOrDefault(teamName, teamName.toLowerCase().replace(" ", "-"));
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