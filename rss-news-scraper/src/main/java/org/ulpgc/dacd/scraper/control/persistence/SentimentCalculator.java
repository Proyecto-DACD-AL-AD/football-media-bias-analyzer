package org.ulpgc.dacd.scraper.control.persistence;

import org.ulpgc.dacd.scraper.control.api.HuggingFaceClient;
import org.ulpgc.dacd.scraper.control.api.SentimentParser;
import org.ulpgc.dacd.scraper.control.config.TokenLoader;
import org.ulpgc.dacd.scraper.model.NewsArticle;

import java.util.HashMap;

public class SentimentCalculator {
    private final HuggingFaceClient sentimentClient;
    private final SentimentParser sentimentParser;
    private final HashMap<String, Double> sentimentCache;

    public SentimentCalculator(){
        String sentimentToken = TokenLoader.loadKey("hf.api.key.sentiment");
        this.sentimentClient = new HuggingFaceClient(sentimentToken);
        this.sentimentParser = new SentimentParser();
        this.sentimentCache = new HashMap<>();
    }

    public double calculateSentiment(NewsArticle article) {
        String textToAnalyze = (article.title() + ". " + article.summary()).trim();

        if (textToAnalyze.equals(".")) return 0.0;
        if (sentimentCache.containsKey(article.link())) return sentimentCache.get(article.link());

        String sentimentJson = sentimentClient.requestModelResponse(textToAnalyze, "cardiffnlp/twitter-xlm-roberta-base-sentiment");
        double score = sentimentParser.parse(sentimentJson);
        sentimentCache.put(article.link(), score);

        return score;
    }

    public NewsArticle addSentimentToArticle(NewsArticle article, double sentimentScore) {
        return new NewsArticle(
                article.title(), article.summary(), article.link(),
                article.pubDate(), article.source(), article.team(),
                sentimentScore, article.ss(), article.ts()
        );
    }
}
