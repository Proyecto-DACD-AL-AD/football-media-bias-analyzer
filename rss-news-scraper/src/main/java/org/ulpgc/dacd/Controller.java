package org.ulpgc.dacd;

import org.ulpgc.dacd.model.NewsArticle;
import org.ulpgc.dacd.persistence.NewsSerializer;
import org.ulpgc.dacd.scraper.NewsScraper;

import java.util.List;

public class Controller {
    private final List<NewsScraper> scrapers;
    private final NewsSerializer serializer;
    private final List<String> teams;

    public Controller(List<NewsScraper> scrapers, NewsSerializer serializer, List<String> teams) {
        this.scrapers = scrapers;
        this.serializer = serializer;
        this.teams = teams;
    }

    public void start() {
        for (NewsScraper scraper : scrapers) {
            for (String team : teams) {
                List<NewsArticle> articles = scraper.feed(team);
                if (!articles.isEmpty()) {
                    serializer.serialize(articles);
                }
            }
        }
    }
}