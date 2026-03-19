package org.ulpgc.dacd.model;

import java.time.Instant;

public record NewsArticle(
        String title,
        String link,
        Instant date,
        String source,
        String team
) {
}
