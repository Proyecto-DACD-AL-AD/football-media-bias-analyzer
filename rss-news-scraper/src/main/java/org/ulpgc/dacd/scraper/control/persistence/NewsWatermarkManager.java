package org.ulpgc.dacd.scraper.control.persistence;

import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NewsWatermarkManager {

    private final Path watermarkFile;
    private final Gson serializer;

    public NewsWatermarkManager(String topicName) {
        this.watermarkFile = Paths.get("state/last_dates_" + topicName + ".json");
        this.serializer = EventSerializer.create();
    }

    public Map<String, Instant> loadLastDates() {
        Map<String, Instant> newsPaperDatesMap = new HashMap<>();
        try {
            if (Files.exists(watermarkFile)) {
                String json = Files.readString(watermarkFile);
                java.lang.reflect.Type stringMapType = new com.google.gson.reflect.TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> rawMap = serializer.fromJson(json, stringMapType);
                if (rawMap != null) {
                    for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                        newsPaperDatesMap.put(entry.getKey(), Instant.parse(entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading the watermark: " + e.getMessage());
        }
        return newsPaperDatesMap;
    }

    public void saveLastDates(Map<String, Instant> lastDatesToSave) {
        try {
            if (watermarkFile.getParent() != null) {
                Files.createDirectories(watermarkFile.getParent());
            }

            String json = serializer.toJson(lastDatesToSave);
            Files.writeString(watermarkFile, json);
        } catch (Exception e) {
            System.err.println("Error writing published news last dates: " + e.getMessage());
        }
    }
}
