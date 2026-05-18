package org.ulpgc.dacd.businessunit.control.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class TeamsProvider {
    private static final String TEAMS_FILE = "media_teams.json";
    private final JsonArray teams;

    public TeamsProvider() {
        this.teams = loadTeams();
    }

    private JsonArray loadTeams() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEAMS_FILE)) {
            if (inputStream == null) {
                System.err.println("Critical error: File " + TEAMS_FILE + " not found in resources.");
                return new JsonArray();
            }
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonArray();
            }
        } catch (Exception e) {
            System.err.println("Error parsing " + TEAMS_FILE + ": " + e.getMessage());
            return new JsonArray();
        }
    }

    public JsonArray getTeams() {
        return teams;
    }
}