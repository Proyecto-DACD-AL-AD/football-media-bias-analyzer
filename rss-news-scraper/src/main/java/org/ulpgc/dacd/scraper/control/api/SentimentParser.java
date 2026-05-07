package org.ulpgc.dacd.scraper.control.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SentimentParser {

    public static double parse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return 0.0;
        }

        try {
            JsonArray results = JsonParser.parseString(jsonResponse).getAsJsonArray().get(0).getAsJsonArray();

            double positive = 0.0;
            double negative = 0.0;

            for (JsonElement element : results) {
                String label = element.getAsJsonObject().get("label").getAsString();
                double score = element.getAsJsonObject().get("score").getAsDouble();

                if (label.equalsIgnoreCase("positive")) {
                    positive = score;
                } else if (label.equalsIgnoreCase("negative")) {
                    negative = score;
                }
            }

            return positive - negative;
        } catch (Exception e) {
            return 0.0;
        }
    }
}