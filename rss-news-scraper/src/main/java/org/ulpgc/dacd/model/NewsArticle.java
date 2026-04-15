package org.ulpgc.dacd.model;

import java.time.Instant;

public record NewsArticle(
        String title,
        String link,
        Instant pubDate,
        String source,
        String team,
        String ss,
        Instant ts
) {
}
