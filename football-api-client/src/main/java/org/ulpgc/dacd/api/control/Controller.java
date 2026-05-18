package org.ulpgc.dacd.api.control;

import org.ulpgc.dacd.api.control.feeder.FootballMatchFeeder;
import org.ulpgc.dacd.api.control.parser.FootballMatchParser;
import org.ulpgc.dacd.api.model.Match;
import org.ulpgc.dacd.api.control.persistence.FootballMatchStore;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            List<Match> matchesToSave = getMatchesWithRank(initialMatches, rankings);
            matchStorer.store(matchesToSave);
        } catch (Exception e) {
            System.err.println("Error in Controller execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<Integer, Map<String, Integer>> fetchRankingsForMatches(List<Match> matches) throws InterruptedException, IOException {
        Map<Integer, Map<String, Integer>> rankingsByMatchday = new HashMap<>();
        Set<Integer> uniqueMatchdays = matches.stream()
                .map(Match::matchday)
                .collect(Collectors.toSet());

        for (int matchday : uniqueMatchdays) {
            System.out.println("Asking for standings of matchday " + matchday + "...");
            String standingsJson = feeder.getStandingsByMatchday(matchday);
            rankingsByMatchday.put(matchday, matchParser.parseStandings(standingsJson));
            Thread.sleep(API_DELAY_MS);
        }
        return rankingsByMatchday;
    }

    private List<Match> getMatchesWithRank(List<Match> matches, Map<Integer, Map<String, Integer>> rankings) {
        return matches.stream()
                .map(match -> setMatchRanks(match, rankings))
                .toList();
    }

    private Match setMatchRanks(Match match, Map<Integer, Map<String, Integer>> rankings) {
        Map<String, Integer> standings = rankings.get(match.matchday());
        if (standings == null) return match;
        int homeRank = standings.getOrDefault(match.homeTeam(), 0);
        int awayRank = standings.getOrDefault(match.awayTeam(), 0);
        return match.addRanks(homeRank, awayRank);
    }
}
