package org.ulpgc.dacd.scrapper.control.persistence;

import org.ulpgc.dacd.scrapper.model.NewsArticle;
import java.util.List;

public interface NewsStore {
    void store(List<NewsArticle> articles);
}