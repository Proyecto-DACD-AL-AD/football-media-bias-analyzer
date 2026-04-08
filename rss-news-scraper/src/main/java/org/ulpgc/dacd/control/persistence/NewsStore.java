package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.NewsArticle;
import java.util.List;

public interface NewsStore {
    void store(List<NewsArticle> articles);
}