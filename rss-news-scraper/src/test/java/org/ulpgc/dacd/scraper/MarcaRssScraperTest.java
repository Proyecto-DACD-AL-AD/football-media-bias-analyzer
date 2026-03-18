package org.ulpgc.dacd.scraper;

import org.junit.Test;
import org.ulpgc.dacd.model.NewsArticle;

import java.util.List;

import static org.junit.Assert.*;

public class MarcaRssScraperTest {

    @Test
    public void shouldReturnArticlesForRealMadrid() {
        NewsScraper scraper = new MarcaRssScraper();
        String team = "Real Madrid";

        List<NewsArticle> articles = scraper.scrape(team);

        assertNotNull(articles);
        assertFalse(articles.isEmpty());

        NewsArticle firstArticle = articles.getFirst();
        assertNotNull(firstArticle.title());
        assertFalse(firstArticle.title().isBlank());
        assertEquals("Marca", firstArticle.source());
        assertEquals(team, firstArticle.team());
    }
}