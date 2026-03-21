package org.ulpgc.dacd;

import org.ulpgc.dacd.model.NewsArticle;
import org.ulpgc.dacd.persistence.NewsRepository;
import org.ulpgc.dacd.persistence.SqliteNewsRepository;
import org.ulpgc.dacd.scraper.AsRssScraper;
import org.ulpgc.dacd.scraper.MarcaRssScraper;
import org.ulpgc.dacd.scraper.MundoDeportivoScraper;
import org.ulpgc.dacd.scraper.NewsScraper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        executeScraping();
    }

    private static void executeScraping() {
        List<NewsScraper> scrapers = Arrays.asList(
                new MarcaRssScraper(),
                new AsRssScraper(),
                new MundoDeportivoScraper()
        );

        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        NewsRepository repository = new SqliteNewsRepository();

        for (NewsScraper scraper : scrapers) {
            for (String team : teams) {
                List<NewsArticle> articles = scraper.scrape(team);
                if (!articles.isEmpty()) {
                    repository.save(articles);
                }
            }
        }
    }

    private static void startScheduledTask() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Main::executeScraping, 0, 6, TimeUnit.HOURS);
    }
}