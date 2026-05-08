package org.ulpgc.dacd.businessunit.control.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class TeamsProvider {
    public JsonArray getTeams() {
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("media_teams.json")))) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (Exception e) {
            return new JsonArray();
        }
    }
}