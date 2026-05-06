package org.ulpgc.dacd.scraper.control.feeder;

import java.util.Map;

public record FeederConfig(
        String sourceName,
        String baseUrl,
        Map<String, String> teamUrlNames
) {}