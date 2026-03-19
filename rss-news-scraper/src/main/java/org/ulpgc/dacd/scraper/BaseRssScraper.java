package org.ulpgc.dacd.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ulpgc.dacd.model.NewsArticle;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseRssScraper implements NewsScraper {
    private final String BASE_URL;
    private final String SOURCE_NAME;
    private final Map<String, String> TEAM_URL_NAMES;

    protected BaseRssScraper(String baseUrl, String sourceName, Map<String, String> teamUrlNames) {
        this.BASE_URL = baseUrl;
        this.SOURCE_NAME = sourceName;
        this.TEAM_URL_NAMES = teamUrlNames;
    }

    @Override
    public final List<NewsArticle> scrape(String teamName) {
        List<NewsArticle> articles = new ArrayList<>();
        String teamSlug = TEAM_URL_NAMES.get(teamName);

        if (teamSlug == null) {
            handleMissingTeam(teamName);
            return articles;
        }

        try {
            Document doc = Jsoup.connect(String.format(BASE_URL, teamSlug)).get();
            Elements items = doc.select("item");
            for (Element item : items) {
                articles.add(parseArticle(item, teamName));
            }
        } catch (IOException e) {
            System.err.println("Error en " + SOURCE_NAME + " para " + teamName + ": " + e.getMessage());
        }
        return articles;
    }

    protected NewsArticle parseArticle(Element item, String teamName) {
        return new NewsArticle(
                extractText(item, "title"),
                extractText(item, "link"),
                parseDate(extractText(item, "pubDate")),
                SOURCE_NAME,
                teamName
        );
    }

    protected void handleMissingTeam(String teamName) {
        System.out.println("[" + SOURCE_NAME + "] Aviso: El equipo '" + teamName + "' no está configurado.");
    }

    private Instant parseDate(String dateStr) {
        return ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    }

    private String extractText(Element item, String tag) {
        Element element = item.selectFirst(tag);
        return element != null ? element.text() : "";
    }
}