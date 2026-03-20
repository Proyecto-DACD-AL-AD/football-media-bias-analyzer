package org.ulpgc.dacd.scraper;

import java.util.Map;

public class MundoDeportivoScraper extends BaseRssScraper {
    private static final String SOURCE_NAME = "Mundo Deportivo";
    private static final String BASE_URL = "https://www.mundodeportivo.com/feed/rss/futbol/%s/";
    private static final Map<String, String> TEAM_URL_NAMES = Map.of(
            "Real Madrid CF", "real-madrid",
            "FC Barcelona", "fc-barcelona",
            "Real Betis Balompié", "betis",
            "Sevilla FC", "sevilla",
            "Real Sociedad de Fútbol", "real-sociedad",
            "Athletic Club", "athletic-bilbao",
            "UD Las Palmas", "ud-las-palmas",
            "CD Tenerife", "tenerife"
    );

    public MundoDeportivoScraper() {
        super(BASE_URL, SOURCE_NAME, TEAM_URL_NAMES);
    }
}
