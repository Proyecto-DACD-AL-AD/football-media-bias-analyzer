package org.ulpgc.dacd.scrapper.model;

import java.time.Instant;

public record NewsArticle(
        String title,
        String summary,
        String link,
        Instant pubDate,
        String source,
        String team,
        String ss,
        Instant ts
) {
}
