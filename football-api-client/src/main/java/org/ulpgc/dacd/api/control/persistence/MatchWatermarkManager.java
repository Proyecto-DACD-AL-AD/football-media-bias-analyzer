package org.ulpgc.dacd.api.control.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class MatchWatermarkManager {
    private final Path watermarkFile;

    public MatchWatermarkManager(String topicName) {
        this.watermarkFile = Paths.get("state/last_date_" + topicName + ".txt");
    }

    public Instant loadLastDate() {
        try {
            if (Files.exists(watermarkFile)) {
                String lastMatchDate = Files.readString(watermarkFile).trim();
                return Instant.parse(lastMatchDate);
            }
        } catch (Exception e) {
            System.err.println("Error reading the watermark: " + e.getMessage());
        }
        return Instant.EPOCH;
    }

    public void saveLastDate(Instant date) {
        try {
            if (watermarkFile.getParent() != null) {
                Files.createDirectories(watermarkFile.getParent());
            }
            Files.writeString(watermarkFile, date.toString());
        } catch (Exception e) {
            System.err.println("Error writing the date: " + e.getMessage());
        }
    }
}
