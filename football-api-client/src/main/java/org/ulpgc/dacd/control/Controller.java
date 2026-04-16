package org.ulpgc.dacd.control;

import org.ulpgc.dacd.control.feeder.FootballMatchFeeder;
import org.ulpgc.dacd.control.filter.FootballMatchFilter;
import org.ulpgc.dacd.model.Match;
import org.ulpgc.dacd.control.persistence.FootballMatchStore;

import java.util.*;

public class Controller {

    private final FootballMatchFeeder feeder;
    private final FootballMatchFilter matchFilter;
    private final FootballMatchStore matchStorer;

    public Controller(FootballMatchFeeder feeder, FootballMatchFilter matchFilter, FootballMatchStore matchStorer) {
        this.feeder = feeder;
        this.matchFilter = matchFilter;
        this.matchStorer = matchStorer;
    }

    public void start() {

        try {
            List<Match> matchesWithoutRank = matchFilter.filterMatches(feeder.getAllMatches());

            Set<Integer> matchdaySet = new HashSet<>();
            for (Match matchToSave : matchesWithoutRank) matchdaySet.add(matchToSave.matchday());


            Map<Integer, Map<String, Integer>> standingsMap = new HashMap<>();

            for (int matchday : matchdaySet) {
                System.out.println("Pidiendo clasificación jornada " + matchday + "...");
                String jsonStandings = feeder.getStandingsByMatchday(matchday);
                standingsMap.put(matchday, matchFilter.parseStandings(jsonStandings));


                Thread.sleep(6000);
            }

            List<Match> matchesToSave = new ArrayList<>();

            for (Match match : matchesWithoutRank) {
                Map<String, Integer> standingOfMatchday = standingsMap.get(match.matchday());

                if (standingOfMatchday != null) {

                    int homeRank = standingOfMatchday.getOrDefault(match.homeTeam(), 0);
                    int awayRank = standingOfMatchday.getOrDefault(match.awayTeam(), 0);
                    matchesToSave.add(match.addRanks(homeRank, awayRank));

                } else {

                    matchesToSave.add(match);
                }
            }

            matchStorer.store(matchesToSave);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
