package org.ulpgc.dacd.control.persistence;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileEventStore implements EventStore {

    private final String baseDirectory;

    public FileEventStore(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void save(String topic, JsonObject event) {
        EventDeserializer metadata = new EventDeserializer(event);
        String path = buildPath(topic, metadata);
        writeToDisk(path, event.toString());
    }

    private String buildPath(String topic, EventDeserializer metadata) {
        String directoryPath = baseDirectory + File.separator +
                topic + File.separator +
                metadata.getSourceSystem();

        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("No se pudo crear el directorio: " + directoryPath);
        }

        return directoryPath + File.separator + metadata.getFormattedDate() + ".events";
    }

    private void writeToDisk(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(content + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}