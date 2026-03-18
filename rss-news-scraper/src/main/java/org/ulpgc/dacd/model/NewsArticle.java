package org.ulpgc.dacd.model;

public record NewsArticle(
        String title,
        String link,
        String date,
        String source,
        String team
) {
}
