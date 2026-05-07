package org.ulpgc.dacd.businessunit.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class EventReader {
    private final String basePath;
    private final String topicName;

    public EventReader(String basePath, String topicName) {
        this.basePath = basePath;
        this.topicName = topicName;
    }

    public void readStore(BiConsumer<String, JsonObject> eventConsumer) {
        Path targetPath = Paths.get(basePath, topicName);
        if (!Files.exists(targetPath)) return;

        try (Stream<Path> paths = Files.walk(targetPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".events"))
                    .forEach(path -> processFile(path, eventConsumer));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void processFile(Path path, BiConsumer<String, JsonObject> eventConsumer) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                eventConsumer.accept(topicName, json);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}