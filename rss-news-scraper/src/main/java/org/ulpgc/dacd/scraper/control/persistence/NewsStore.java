package org.ulpgc.dacd.scraper.control.persistence;

import org.ulpgc.dacd.scraper.model.NewsArticle;
import java.util.List;

public interface NewsStore {
    void store(List<NewsArticle> articles);
}