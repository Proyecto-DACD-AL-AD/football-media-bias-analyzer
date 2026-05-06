package org.ulpgc.dacd.feeder;

import org.junit.Test;
import org.ulpgc.dacd.scraper.control.feeder.NewsScraper;
import org.ulpgc.dacd.scraper.control.feeder.RssScraper;
import org.ulpgc.dacd.scraper.model.NewsArticle;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MundoDeportivoScraperTest {

    @Test
    public void shouldReturnArticlesForAllTeams() {
        NewsScraper scraper = new RssScraper("feeders/mundo_deportivo_config.json");
        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        for (String team : teams) {
            List<NewsArticle> articles = scraper.feed(team);

            assertNotNull(articles);
            assertFalse("Debería haber noticias en Mundo Deportivo para " + team, articles.isEmpty());

            NewsArticle firstArticle = articles.getFirst();
            assertNotNull(firstArticle.title());
            assertFalse(firstArticle.title().isBlank());
            assertEquals("Mundo Deportivo", firstArticle.source());
            assertEquals(team, firstArticle.team());
        }
    }
}