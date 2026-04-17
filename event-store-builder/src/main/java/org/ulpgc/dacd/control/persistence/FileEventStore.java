package org.ulpgc.dacd.control.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileEventStore implements EventStore {

    private final String baseDirectory;

    public FileEventStore(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void save(String topic, String eventJson) {
        EventDeserializer metadata = new EventDeserializer(eventJson);
        String path = buildPath(topic, metadata);
        writeToDisk(path, eventJson);
    }

    private String buildPath(String topic, EventDeserializer metadata) {
        String directoryPath = baseDirectory + File.separator +
                topic + File.separator +
                metadata.getSourceSystem();

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
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