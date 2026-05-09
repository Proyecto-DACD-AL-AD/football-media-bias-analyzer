package org.ulpgc.dacd.scraper.control.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class TeamLoader {
    public static List<String> load(String filePath) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TeamLoader.class.getClassLoader().getResourceAsStream(filePath)))) {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}