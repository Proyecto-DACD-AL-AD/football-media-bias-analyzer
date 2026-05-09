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
            double positiveScore = 0.0;
            double negativeScore = 0.0;

            for (JsonElement result : results) {
                JsonObject prediction = result.getAsJsonObject();
                String label = prediction.get("label").getAsString();
                double score = prediction.get("score").getAsDouble();

                if (label.equalsIgnoreCase(POSITIVE_LABEL)) positiveScore = score;
                else if (label.equalsIgnoreCase(NEGATIVE_LABEL)) negativeScore = score;
            }
            return positiveScore - negativeScore;
        } catch (Exception e) {
            return 0.0;
        }
    }
}