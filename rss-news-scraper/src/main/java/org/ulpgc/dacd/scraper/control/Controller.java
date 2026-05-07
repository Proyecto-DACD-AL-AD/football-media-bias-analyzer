package org.ulpgc.dacd.scraper.control;

import org.ulpgc.dacd.scraper.model.NewsArticle;
import org.ulpgc.dacd.scraper.control.persistence.NewsStore;
import org.ulpgc.dacd.scraper.control.feeder.NewsScraper;

import java.util.List;

public class Controller {
    private final List<NewsScraper> scrapers;
    private final NewsStore storer;
    private final List<String> teams;

    public Controller(List<NewsScraper> scrapers, NewsStore store, List<String> teams) {
        this.scrapers = scrapers;
        this.storer = store;
        this.teams = teams;
    }

    public void start() {
        for (NewsScraper scraper : scrapers) {
            for (String team : teams) {
                List<NewsArticle> articles = scraper.feed(team);
                if (!articles.isEmpty()) {
                    storer.store(articles);
                }
            }
        }
    }
}