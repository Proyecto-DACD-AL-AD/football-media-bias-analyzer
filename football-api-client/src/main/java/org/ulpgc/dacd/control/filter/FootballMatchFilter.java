package org.ulpgc.dacd.control.filter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.ulpgc.dacd.model.Match;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;

public class FootballMatchFilter {

    private static final List<String> MEDIA_TEAMS = loadTeams("media_teams.json") ;

    public static List<String> loadTeams(String filePath) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(FootballMatchFilter.class.getClassLoader().getResourceAsStream(filePath)))) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Match> filterMatches(String allMatchesJson) {

        if (isJsonEmpty(allMatchesJson)) return new ArrayList<>();

        List<Match> filteredMatches = new ArrayList<>();
        JsonObject root = JsonParser.parseString(allMatchesJson).getAsJsonObject();
        JsonArray matchesArray = root.getAsJsonArray("matches");

        for (JsonElement element : matchesArray) {
            JsonObject matchJson = element.getAsJsonObject();


            String status = matchJson.get("status").getAsString();
            if (!"FINISHED".equals(status)) continue;


            String homeTeam = matchJson.getAsJsonObject("homeTeam").get("name").getAsString();
            String awayTeam = matchJson.getAsJsonObject("awayTeam").get("name").getAsString();

            if (!MEDIA_TEAMS.contains(homeTeam) && !MEDIA_TEAMS.contains(awayTeam)) continue;


            Instant date = Instant.parse(matchJson.get("utcDate").getAsString());
            int matchday = matchJson.get("matchday").getAsInt();

            JsonObject fullTime = matchJson.getAsJsonObject("score").getAsJsonObject("fullTime");
            int homeGoals = fullTime.get("home").getAsInt();
            int awayGoals = fullTime.get("away").getAsInt();

            Match match = new Match(date, matchday, homeTeam, awayTeam, homeGoals, awayGoals, 0, 0);
            filteredMatches.add(match);
        }

        // printFilteredMatches(filteredMatches);
        return filteredMatches;
    }

    public Map<String, Integer> parseStandings(String standingsJson) {
        Map<String, Integer> standingsMap = new HashMap<>();
        if (isJsonEmpty(standingsJson)) return standingsMap;

        JsonObject root = JsonParser.parseString(standingsJson).getAsJsonObject();
        if (!root.has("standings")) return standingsMap;

        JsonArray standingsArray = root.getAsJsonArray("standings");

        for (JsonElement element : standingsArray) {
            JsonObject standing = element.getAsJsonObject();


            if ("TOTAL".equals(standing.get("type").getAsString())) {
                JsonArray table = standing.getAsJsonArray("table");


                for (JsonElement rowElement : table) {
                    JsonObject row = rowElement.getAsJsonObject();
                    String teamName = row.getAsJsonObject("team").get("name").getAsString();
                    int teamRank = row.get("position").getAsInt();

                    standingsMap.put(teamName, teamRank);
                }
                break;
            }
        }
        return standingsMap;
    }

    private static boolean isJsonEmpty(String rawJson) {
        return rawJson == null || rawJson.trim().equals("{}") || rawJson.trim().isEmpty();
    }

    private static void printFilteredMatches(List<Match> filteredMatches) {
        for (Match match : filteredMatches) {
            String result = match.homeGoals() + " - " + match.awayGoals();
            System.out.println(match.homeTeam() + " [" + result + "] " + match.awayTeam() + " | Jornada: " + match.matchday());
        }
    }
}