package org.ulpgc.dacd.control.feeder;

import java.util.List;
import org.ulpgc.dacd.model.NewsArticle;

public interface NewsScraper {
    List<NewsArticle> feed(String teamName);
}
