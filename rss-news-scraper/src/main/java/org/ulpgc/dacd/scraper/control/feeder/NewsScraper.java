package org.ulpgc.dacd.scraper.control.feeder;

import java.util.List;
import org.ulpgc.dacd.scraper.model.NewsArticle;

public interface NewsScraper {
    List<NewsArticle> feed(String teamName);
}
