package org.ulpgc.dacd.api.control.filter;

import com.google.gson.*;
import org.ulpgc.dacd.api.model.Match;

import java.time.Instant;
import java.util.*;

public class FootballMatchParser {
    public List<Match> parseMatches(String rawMatchesJson) {
        if (isJsonEmpty(rawMatchesJson)) return new ArrayList<>();
        JsonArray matchesArray = extractMatchesArray(rawMatchesJson);
        List<Match> parsedMatches = new ArrayList<>();

        for (JsonElement rawMatch : matchesArray) {
            JsonObject matchJson = rawMatch.getAsJsonObject();
            if (isFinished(matchJson)) {
                parsedMatches.add(mapToMatch(matchJson));
            }
        }
        return parsedMatches;
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
            for (JsonElement standing: standingsArray) {
                JsonObject standingJson = standing.getAsJsonObject();
                if (isTotalType(standingJson)) {
                    fillRankingsFromTable(standingJson.getAsJsonArray("table"), teamRankings);
                    break;
                }
            }
        }
        return teamRankings;
    }

    private boolean isTotalType(JsonObject standing) {
        return "TOTAL".equals(standing.get("type").getAsString());
    }

    private void fillRankingsFromTable(JsonArray table, Map<String, Integer> teamRankings) {
        for (JsonElement position : table) {
            JsonObject row = position.getAsJsonObject();
            String teamName = row.getAsJsonObject("team").get("name").getAsString();
            int teamPosition = row.get("position").getAsInt();
            teamRankings.put(teamName, teamPosition);
        }
    }

    private static boolean isJsonEmpty(String rawJson) {
        return rawJson == null || rawJson.trim().equals("{}") || rawJson.trim().isEmpty();
    }

}