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

    public JsonArray getTeamEvolutionData(String teamName) {
        return teamEvolutionQuery.execute(teamName);
    }

    public JsonArray getSourceSentimentData(String teamName) {
        return sourceSentimentQuery.execute(teamName);
    }

    public JsonArray getSentimentCorrelationData(String teamName) {
        return rankSentimentCorrelationQuery.execute(teamName);
    }

    public JsonArray getGlobalStatsData() {
        return globalStatsQuery.execute();
    }
}