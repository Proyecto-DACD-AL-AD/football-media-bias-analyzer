package org.ulpgc.dacd.scraper;

import java.util.Map;

public class MarcaRssScraper extends BaseRssScraper {
    private static final String SOURCE_NAME = "Marca";
    private static final String BASE_URL = "https://objetos.estaticos-marca.com/rss/futbol/%s.xml";
    private static final Map<String, String> TEAM_URL_NAMES = Map.of(
            "Real Madrid CF", "real-madrid",
            "FC Barcelona", "barcelona",
            "Real Betis Balompié", "betis",
            "Sevilla FC", "sevilla",
            "Real Sociedad de Fútbol", "real-sociedad",
            "Athletic Club", "athletic",
            "UD Las Palmas", "las-palmas",
            "CD Tenerife", "tenerife"
    );

    public MarcaRssScraper() {
        super(BASE_URL, SOURCE_NAME, TEAM_URL_NAMES);
    }
}