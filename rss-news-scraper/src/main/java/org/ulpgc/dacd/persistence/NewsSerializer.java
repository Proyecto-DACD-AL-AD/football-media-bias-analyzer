package org.ulpgc.dacd.persistence;

import org.ulpgc.dacd.model.NewsArticle;
import java.util.List;

public interface NewsSerializer {
    void serialize(List<NewsArticle> articles);
}