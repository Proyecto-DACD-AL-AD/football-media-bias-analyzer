package org.ulpgc.dacd;

import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.feeder.NewsScraper;
import org.ulpgc.dacd.control.feeder.RssScraper;
import org.ulpgc.dacd.control.persistence.DatabaseNewsStore;
import org.ulpgc.dacd.control.persistence.NewsStore;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<NewsScraper> feeders = Arrays.asList(
                new RssScraper("feeders/marca_config.json"),
                new RssScraper("feeders/as_config.json"),
                new RssScraper("feeders/mundo_deportivo_config.json")
        );

        NewsStore store = new DatabaseNewsStore();

        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        Controller controller = new Controller(feeders, store, teams);
        controller.start();
    }
}