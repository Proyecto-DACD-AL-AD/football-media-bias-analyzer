package org.ulpgc.dacd.scraper.control.api;

import com.google.gson.*;

public class SentimentParser {
    private static final String POSITIVE_LABEL = "positive";
    private static final String NEGATIVE_LABEL = "negative";

    public double parse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) return 0.0;
        try {
            JsonArray results = JsonParser.parseString(jsonResponse)
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonArray();

            return results.asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .mapToDouble(this::calculateWeightedScore)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private double calculateWeightedScore(JsonObject prediction) {
        String label = prediction.get("label").getAsString();
        double score = prediction.get("score").getAsDouble();
        if (label.equalsIgnoreCase(POSITIVE_LABEL)) return score;
        if (label.equalsIgnoreCase(NEGATIVE_LABEL)) return -score;
        return 0.0;
    }
}