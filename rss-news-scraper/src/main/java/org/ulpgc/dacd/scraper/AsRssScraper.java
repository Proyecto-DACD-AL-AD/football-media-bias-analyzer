package org.ulpgc.dacd.scraper;

import java.util.Map;

public class AsRssScraper extends BaseRssScraper {
    private static final String SOURCE_NAME = "AS";
    private static final String BASE_URL = "https://feeds.as.com/mrss-s/list/as/site/as.com/tag/%s_a/";
    private static final Map<String, String> TEAM_URL_NAMES = Map.of(
            "Real Madrid CF", "real_madrid",
            "FC Barcelona", "barcelona",
            "Real Betis Balompié", "real_betis",
            "Sevilla FC", "sevilla_futbol_club",
            "Real Sociedad de Fútbol", "real_sociedad",
            "Athletic Club", "athletic",
            "UD Las Palmas", "las_palmas",
            "CD Tenerife", "cd_tenerife"
    );

    public AsRssScraper() {
        super(BASE_URL, SOURCE_NAME, TEAM_URL_NAMES);
    }
}