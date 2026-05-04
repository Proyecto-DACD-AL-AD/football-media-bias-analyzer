package org.ulpgc.dacd.feeder;

import org.junit.Test;
import org.ulpgc.dacd.scrapper.control.feeder.NewsScraper;
import org.ulpgc.dacd.scrapper.control.feeder.RssScraper;
import org.ulpgc.dacd.scrapper.model.NewsArticle;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AsRssScraperTest {

    @Test
    public void shouldReturnArticlesForAllTeams() {
        NewsScraper scraper = new RssScraper("feeders/as_config.json");
        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        for (String team : teams) {
            List<NewsArticle> articles = scraper.feed(team);

            assertNotNull(articles);
            assertFalse("Debería haber noticias en AS para " + team, articles.isEmpty());

            NewsArticle firstArticle = articles.getFirst();
            assertNotNull(firstArticle.title());
            assertFalse(firstArticle.title().isBlank());
            assertEquals("AS", firstArticle.source());
            assertEquals(team, firstArticle.team());
        }
    }
}