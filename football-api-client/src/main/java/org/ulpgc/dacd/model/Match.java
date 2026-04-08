package org.ulpgc.dacd.model;
import java.time.Instant;

public record Match(
        Instant date,
        int matchday,
        String homeTeam,
        String awayTeam,
        int homeGoals,
        int awayGoals,
        int homeRankAfterMatchday,
        int awayRankAfterMatchday) {

    public Match addRanks(int homeRank, int awayRank) {
        return new Match(date, matchday, homeTeam, awayTeam, homeGoals, awayGoals, homeRank,
                awayRank);
    }
}
