package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonArray;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.queries.GlobalStatsQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.SourceSentimentQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.RankSentimentCorrelationQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.TeamEvolutionQuery;

public class DashboardRepository {
    private final TeamEvolutionQuery teamEvolutionQuery;
    private final SourceSentimentQuery sourceSentimentQuery;
    private final RankSentimentCorrelationQuery rankSentimentCorrelationQuery;
    private final GlobalStatsQuery globalStatsQuery;

    public DashboardRepository(DatabaseManager dbManager) {
        this.teamEvolutionQuery = new TeamEvolutionQuery(dbManager);
        this.sourceSentimentQuery = new SourceSentimentQuery(dbManager);
        this.rankSentimentCorrelationQuery = new RankSentimentCorrelationQuery(dbManager);
        this.globalStatsQuery = new GlobalStatsQuery(dbManager);
    }

    public JsonArray getThermometerData(String team) {
        return teamEvolutionQuery.execute(team);
    }

    public JsonArray getRadarData(String team) {
        return sourceSentimentQuery.execute(team);
    }

    public JsonArray getScatterData(String team) {
        return rankSentimentCorrelationQuery.execute(team);
    }

    public JsonArray getGlobalStats() {
        return globalStatsQuery.execute();
    }
}