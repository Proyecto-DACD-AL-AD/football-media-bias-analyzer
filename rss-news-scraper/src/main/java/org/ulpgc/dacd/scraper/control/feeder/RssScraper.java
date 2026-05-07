package org.ulpgc.dacd.scraper.control.feeder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ulpgc.dacd.scraper.control.api.HuggingFaceClient;
import org.ulpgc.dacd.scraper.control.api.SentimentParser;
import org.ulpgc.dacd.scraper.control.config.TokenLoader;
import org.ulpgc.dacd.scraper.model.NewsArticle;

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
    private final HuggingFaceClient sentimentClient;

    public RssScraper(String configFilePath) {
        try (java.io.Reader reader = new java.io.InputStreamReader(
                java.util.Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(configFilePath)))) {

            com.google.gson.Gson gson = new com.google.gson.Gson();
            FeederConfig config = gson.fromJson(reader, FeederConfig.class);

            this.baseUrl = config.baseUrl();
            this.sourceName = config.sourceName();
            this.teamUrlNames = config.teamUrlNames();

            String sentimentToken = TokenLoader.loadKey("hf.api.key.sentiment");
            this.sentimentClient = new HuggingFaceClient(sentimentToken);

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
        String ss = "rss-scraper";
        Instant ts = Instant.now();

        String title = extractText(item, "title");
        String rawDescription = extractText(item, "description");
        String cleanSummary = cleanSummary(rawDescription);

        String textToAnalyze = (title + ". " + cleanSummary).trim();
        double finalSentimentScore = 0.0;

        if (!textToAnalyze.equals(".")) {
            String sentimentJson = sentimentClient.analyze(textToAnalyze, "cardiffnlp/twitter-xlm-roberta-base-sentiment");
            finalSentimentScore = SentimentParser.parse(sentimentJson);
        }

        return new NewsArticle(
                extractText(item, "title"),
                cleanSummary,
                extractText(item, "link"),
                parseDate(extractText(item, "pubDate")),
                sourceName,
                teamName,
                finalSentimentScore,
                ss,
                ts
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

    private String cleanSummary(String rawSummary) {
        if (rawSummary == null || rawSummary.isEmpty()) {
            return "";
        }

        Document fragment = Jsoup.parseBodyFragment(rawSummary);
        fragment.select("a, img").remove();

        return fragment.text()
                .replace("&nbsp;", " ")
                .replace("\u00A0", " ")
                .trim();
    }
}