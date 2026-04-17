package org.ulpgc.dacd;

import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.feeder.NewsScraper;
import org.ulpgc.dacd.control.feeder.RssScraper;
import org.ulpgc.dacd.control.persistence.NewsPublisher;
import org.ulpgc.dacd.control.persistence.NewsStore;
import org.ulpgc.dacd.control.config.TeamLoader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        List<NewsScraper> feeders = Arrays.asList(
                new RssScraper("feeders/marca_config.json"),
                new RssScraper("feeders/as_config.json"),
                new RssScraper("feeders/mundo_deportivo_config.json")
        );

        NewsStore store = new NewsPublisher("tcp://localhost:61616", "news");
        List<String> teams = TeamLoader.load("media_teams.json");

        Controller controller = new Controller(feeders, store, teams);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        System.out.println("Iniciando el recolector de noticias RSS...");

        scheduler.scheduleAtFixedRate(controller::start, 0, 12, TimeUnit.HOURS);
    }
}