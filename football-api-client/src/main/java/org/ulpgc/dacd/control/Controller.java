package org.ulpgc.dacd.control;

import org.ulpgc.dacd.control.feeder.FootballMatchFeeder;
import org.ulpgc.dacd.control.filter.FootballMatchFilter;
import org.ulpgc.dacd.model.MatchResponse;
import org.ulpgc.dacd.control.persistence.FootballMatchStore;

import java.util.*;

public class Controller {

    private final FootballMatchFeeder feeder;
    private final FootballMatchFilter matchFilter;
    private final FootballMatchStore serializer;

    public Controller(FootballMatchFeeder feeder, FootballMatchFilter matchFilter, FootballMatchStore serializer) {
        this.feeder = feeder;
        this.matchFilter = matchFilter;
        this.serializer = serializer;
    }

    public void start() {

        try {
            List<MatchResponse> matchesToSave = matchFilter.filterMatches(feeder.getAllMatches());

            Set<Integer> matchdaySet = new HashSet<>();
            for (MatchResponse matchToSave : matchesToSave) matchdaySet.add(matchToSave.getMatchday());


            Map<Integer, Map<String, Integer>> standingsMap = new HashMap<>();

            for (int matchday : matchdaySet) {
                System.out.println("Pidiendo clasificación jornada " + matchday + "...");
                String jsonStandings = feeder.getStandingsByMatchday(matchday);
                standingsMap.put(matchday, matchFilter.parseStandings(jsonStandings));


                Thread.sleep(6000);
            }

            for (MatchResponse match : matchesToSave) {
                Map<String, Integer> standingOfMatchday = standingsMap.get(match.getMatchday());
                if (standingOfMatchday != null) {
                    match.setHomeRankAfterMatchday(standingOfMatchday.get(match.getHomeTeam().getName()));
                    match.setAwayRankAfterMatchday(standingOfMatchday.get(match.getAwayTeam().getName()));
                }
            }

            serializer.createTable();
            serializer.insertMatches(matchesToSave);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
