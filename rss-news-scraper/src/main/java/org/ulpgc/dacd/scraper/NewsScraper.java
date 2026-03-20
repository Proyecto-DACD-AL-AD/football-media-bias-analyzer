package org.ulpgc.dacd.scraper;

import java.util.List;
import org.ulpgc.dacd.model.NewsArticle;

public interface NewsScraper {
    List<NewsArticle> scrape(String teamName);
}
