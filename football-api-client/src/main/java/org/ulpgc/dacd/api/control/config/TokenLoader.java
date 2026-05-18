package org.ulpgc.dacd.api.control.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TokenLoader {
    private static final String FILE_PATH = "application.properties";

    public static String loadKey(String keyName) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(FILE_PATH)) {
            properties.load(input);
            String key = properties.getProperty(keyName);
            if (key == null || key.isEmpty()) {
                throw new RuntimeException("The key '" + keyName + "' does not exists on 'application.properties'");
            }
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Tokens file could not be read: " + e.getMessage());
        }
    }
}