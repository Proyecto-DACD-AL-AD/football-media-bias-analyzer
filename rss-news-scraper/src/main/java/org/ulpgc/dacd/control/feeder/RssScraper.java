package org.ulpgc.dacd.control.feeder;

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

public class RssScraper implements NewsScraper {
    private final String baseUrl;
    private final String sourceName;
    private final Map<String, String> teamUrlNames;

    public RssScraper(String configFilePath) {
        try (java.io.Reader reader = new java.io.InputStreamReader(
                java.util.Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(configFilePath)))) {

            com.google.gson.Gson gson = new com.google.gson.Gson();
            FeederConfig config = gson.fromJson(reader, FeederConfig.class);

            this.baseUrl = config.baseUrl();
            this.sourceName = config.sourceName();
            this.teamUrlNames = config.teamUrlNames();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final List<NewsArticle> feed(String teamName) {
        List<NewsArticle> articles = new ArrayList<>();
        String teamSlug = teamUrlNames.get(teamName);

        if (teamSlug == null) {
            handleMissingTeam(teamName);
            return articles;
        }

        try {
            Document doc = Jsoup.connect(String.format(baseUrl, teamSlug)).get();
            Elements items = doc.select("item");
            for (Element item : items) {
                articles.add(parseArticle(item, teamName));
            }
        } catch (IOException e) {
            System.err.println("Error en " + sourceName + " para " + teamName + ": " + e.getMessage());
        }
        return articles;
    }

    protected NewsArticle parseArticle(Element item, String teamName) {
        return new NewsArticle(
                extractText(item, "title"),
                extractText(item, "link"),
                parseDate(extractText(item, "pubDate")),
                sourceName,
                teamName
        );
    }

    protected void handleMissingTeam(String teamName) {
        System.out.println("[" + sourceName + "] Aviso: El equipo '" + teamName + "' no está configurado.");
    }

    private Instant parseDate(String dateStr) {
        return ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    }

    private String extractText(Element item, String tag) {
        Element element = item.selectFirst(tag);
        return element != null ? element.text() : "";
    }
}