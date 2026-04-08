package org.ulpgc.dacd.control.filter;

import com.google.gson.Gson;
import org.ulpgc.dacd.model.*;

import java.util.*;

public class FootballMatchFilter {

    public List<MatchResponse> filterMatches(String allMatchesJson) {

        if (isJsonEmpty(allMatchesJson)) return new ArrayList<>();

        Gson gson = new Gson();
        MatchListResponse matchList = gson.fromJson(allMatchesJson, MatchListResponse.class);
        List<String> mediaTeams = List.of("Real Madrid CF", "FC Barcelona",
                "Real Betis Balompié", "Sevilla FC", "Athletic Club", "Real Sociedad de Fútbol");

        List<MatchResponse> filteredMatches = matchList.getMatches().stream()
                .filter(match -> "FINISHED".equals(match.getStatus()))
                .filter(match ->
                        mediaTeams.contains(match.getHomeTeam().getName()) ||
                                mediaTeams.contains(match.getAwayTeam().getName())
                ).toList();

        printFilteredMatches(filteredMatches);
        return filteredMatches;
    }

    private static boolean isJsonEmpty(String rawJson) {
        return rawJson.equals("{}");
    }

    private static void printFilteredMatches(List<MatchResponse> filteredMatches) {

        for (MatchResponse match : filteredMatches) {
            String homeTeam = match.getHomeTeam().getName();
            String awayTeam = match.getAwayTeam().getName();

            int homeGoals = match.getScore().getFullTime().getHomeGoals();
            int awayGoals = match.getScore().getFullTime().getAwayGoals();
            String result = homeGoals + " - " + awayGoals;

            System.out.println(homeTeam + " [" + result + "] " + awayTeam + " | Status: " + match.getStatus());
        }
    }

    public Map<String, Integer> parseStandings(String standingsJson) {
        Gson gson = new Gson();

        StandingsResponse standingsResponse = gson.fromJson(standingsJson, StandingsResponse.class);
        Map<String, Integer> standingsMap = new HashMap<>();

        if (standingsResponse != null && standingsResponse.getStandings() != null) {

            for (Standing standing : standingsResponse.getStandings()) {
                if ("TOTAL".equals(standing.getType())) {

                    for (TableEntry entry : standing.getTable()) {
                        standingsMap.put(entry.getTeam().getName(), entry.getPosition());
                    }
                    break;
                }
            }
        }
        return standingsMap;
    }

}
