package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.NewsArticle;
import org.ulpgc.dacd.control.persistence.NewsStore;
import org.ulpgc.dacd.control.feeder.NewsScraper;

import java.util.List;

public class Controller {
    private final List<NewsScraper> scrapers;
    private final NewsStore serializer;
    private final List<String> teams;

    public Controller(List<NewsScraper> scrapers, NewsStore serializer, List<String> teams) {
        this.scrapers = scrapers;
        this.serializer = serializer;
        this.teams = teams;
    }

    public void start() {
        for (NewsScraper scraper : scrapers) {
            for (String team : teams) {
                List<NewsArticle> articles = scraper.feed(team);
                if (!articles.isEmpty()) {
                    serializer.store(articles);
                }
            }
        }
    }
}