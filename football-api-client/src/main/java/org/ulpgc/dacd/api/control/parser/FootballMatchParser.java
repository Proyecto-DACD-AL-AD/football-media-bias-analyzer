package org.ulpgc.dacd.api.control.parser;

import com.google.gson.*;
import org.ulpgc.dacd.api.model.Match;

import java.time.Instant;
import java.util.*;

public class FootballMatchParser {
    public List<Match> parseMatches(String rawMatchesJson) {
        if (isJsonEmpty(rawMatchesJson)) return new ArrayList<>();
        JsonArray matchesArray = extractMatchesArray(rawMatchesJson);
        return matchesArray.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .filter(this::isFinished)
                .map(this::mapToMatch)
                .toList();
    }

    private JsonArray extractMatchesArray(String json) {
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .getAsJsonArray("matches");
    }

    private boolean isFinished(JsonObject matchJson) {
        return "FINISHED".equals(matchJson.get("status").getAsString());
    }

    private Match mapToMatch(JsonObject matchJson) {
        JsonObject fullTime = matchJson.getAsJsonObject("score").getAsJsonObject("fullTime");
        return new Match(
                Instant.parse(matchJson.get("utcDate").getAsString()),
                matchJson.get("matchday").getAsInt(),
                matchJson.getAsJsonObject("homeTeam").get("name").getAsString(),
                matchJson.getAsJsonObject("awayTeam").get("name").getAsString(),
                fullTime.get("home").getAsInt(),
                fullTime.get("away").getAsInt(),
                0,
                0,
                "football-api",
                Instant.now()
        );
    }

    public Map<String, Integer> parseStandings(String rawStandingsJson) {
        if (isJsonEmpty(rawStandingsJson)) return new HashMap<>();
        JsonObject standingsJson = JsonParser.parseString(rawStandingsJson).getAsJsonObject();
        return extractTotalStandings(standingsJson);
    }

    private Map<String, Integer> extractTotalStandings(JsonObject standingsJson) {
        Map<String, Integer> teamRankings = new HashMap<>();
        if (standingsJson.has("standings")) {
            JsonArray standingsArray = standingsJson.getAsJsonArray("standings");
            standingsArray.asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .filter(this::isTotalType)
                    .forEach(standing -> fillRankingsFromTable(standing.getAsJsonArray("table"), teamRankings));
        }
        return teamRankings;
    }

    private boolean isTotalType(JsonObject standing) {
        return "TOTAL".equals(standing.get("type").getAsString());
    }

    private void fillRankingsFromTable(JsonArray table, Map<String, Integer> teamRankings) {
        table.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .forEach(row -> {
                    String teamName = row.getAsJsonObject("team").get("name").getAsString();
                    int teamPosition = row.get("position").getAsInt();
                    teamRankings.put(teamName, teamPosition);
                });
    }

    private static boolean isJsonEmpty(String rawJson) {
        return rawJson == null || rawJson.trim().equals("{}") || rawJson.trim().isEmpty();
    }
}