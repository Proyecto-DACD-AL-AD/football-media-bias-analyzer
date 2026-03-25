package org.ulpgc.dacd;

import org.ulpgc.dacd.persistence.DatabaseNewsSerializer;
import org.ulpgc.dacd.persistence.NewsSerializer;
import org.ulpgc.dacd.scraper.*;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<NewsScraper> scrapers = Arrays.asList(
                new MarcaRssScraper(),
                new AsRssScraper(),
                new MundoDeportivoScraper()
        );

        NewsSerializer serializer = new DatabaseNewsSerializer();

        List<String> teams = Arrays.asList(
                "Real Madrid CF", "FC Barcelona", "Real Betis Balompié", "Sevilla FC",
                "Real Sociedad de Fútbol", "Athletic Club", "UD Las Palmas", "CD Tenerife"
        );

        Controller controller = new Controller(scrapers, serializer, teams);
        controller.start();
    }
}