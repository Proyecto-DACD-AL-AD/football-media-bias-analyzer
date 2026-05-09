package org.ulpgc.dacd.api.control;

import org.ulpgc.dacd.api.control.feeder.FootballMatchFeeder;
import org.ulpgc.dacd.api.control.filter.FootballMatchParser;
import org.ulpgc.dacd.api.model.Match;
import org.ulpgc.dacd.api.control.persistence.FootballMatchStore;

import java.io.IOException;
import java.util.*;

public class Controller {
    private static final int API_DELAY_MS = 6000;
    private final FootballMatchFeeder feeder;
    private final FootballMatchParser matchParser;
    private final FootballMatchStore matchStorer;

    public Controller(FootballMatchFeeder feeder, FootballMatchParser matchParser, FootballMatchStore matchStorer) {
        this.feeder = feeder;
        this.matchParser = matchParser;
        this.matchStorer = matchStorer;
    }

    public void start() {
        try {
            List<Match> initialMatches = matchParser.parseMatches(feeder.getAllMatches());
            Map<Integer, Map<String, Integer>> rankings = fetchRankingsForMatches(initialMatches);
            List<Match> matchesToSave = setMatchRankings(initialMatches, rankings);
            matchStorer.store(matchesToSave);

        } catch (Exception e) {
            System.err.println("Error in Controller execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<Integer, Map<String, Integer>> fetchRankingsForMatches(List<Match> matches) throws InterruptedException, IOException {
        Map<Integer, Map<String, Integer>> rankingsByMatchday = new HashMap<>();
        Set<Integer> uniqueMatchdays = new HashSet<>();
        for (Match match : matches) uniqueMatchdays.add(match.matchday());

        for (int matchday : uniqueMatchdays) {
            System.out.println("Asking for standings of matchday " + matchday + "...");
            String standingsJson = feeder.getStandingsByMatchday(matchday);
            rankingsByMatchday.put(matchday, matchParser.parseStandings(standingsJson));
            Thread.sleep(API_DELAY_MS);
        }
        return rankingsByMatchday;
    }

    private List<Match> setMatchRankings(List<Match> matches, Map<Integer, Map<String, Integer>> rankings) {
        List<Match> matchesWithRank = new ArrayList<>();
        for (Match match : matches) {
            Map<String, Integer> standings = rankings.get(match.matchday());
            if (standings != null) {
                int homeRank = standings.getOrDefault(match.homeTeam(), 0);
                int awayRank = standings.getOrDefault(match.awayTeam(), 0);
                matchesWithRank.add(match.addRanks(homeRank, awayRank));
            } else {
                matchesWithRank.add(match);
            }
        }
        return matchesWithRank;
    }
}
