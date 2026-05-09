package org.ulpgc.dacd.eventstore.control.persistence;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileEventStore implements EventStore {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd")
            .withZone(ZoneId.of("UTC"));

    private final String baseDirectory;

    public FileEventStore(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void save(String topic, JsonObject event) {
        String path = buildPath(topic, event);
        writeToDisk(path, event.toString());
    }

    private String buildPath(String topic, JsonObject event) {
        String ss = event.get("ss").getAsString();
        String tsString = event.get("ts").getAsString();
        Instant timestamp = Instant.parse(tsString);
        String formattedDate = DATE_FORMATTER.format(timestamp);
        String directoryPath = baseDirectory + File.separator +
                topic + File.separator +
                ss;

        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("The directory could not be created: " + directoryPath);
        }
        return directoryPath + File.separator + formattedDate + ".events";
    }

    private void writeToDisk(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(content + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}