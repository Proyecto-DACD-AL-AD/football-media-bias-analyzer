package org.ulpgc.dacd.scraper;

import org.junit.Test;
import org.ulpgc.dacd.model.NewsArticle;

import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MarcaRssScraperTest {

    @Test
    public void shouldReturnArticlesForAllTeams() {
        NewsScraper scraper = new MarcaRssScraper();
        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        for (String team : teams) {
            List<NewsArticle> articles = scraper.scrape(team);

            assertNotNull(articles);
            assertFalse("Debería haber noticias en Marca para " + team, articles.isEmpty());

            NewsArticle firstArticle = articles.getFirst();
            assertNotNull(firstArticle.title());
            assertFalse(firstArticle.title().isBlank());
            assertEquals("Marca", firstArticle.source());
            assertEquals(team, firstArticle.team());
        }
    }
}