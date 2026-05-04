package org.ulpgc.dacd.scrapper.control.feeder;

import java.util.List;
import org.ulpgc.dacd.scrapper.model.NewsArticle;

public interface NewsScraper {
    List<NewsArticle> feed(String teamName);
}
