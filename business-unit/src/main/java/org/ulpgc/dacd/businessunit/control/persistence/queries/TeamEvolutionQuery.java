package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class TeamEvolutionQuery {
    private static final int PARAMETER_COUNT = 6;
    private final DatabaseManager dbManager;
    private static final String SQL_QUERY = """
            WITH RECURSIVE
            MinMaxDates AS (
                SELECT MIN(substr(date, 1, 10)) as min_date, MAX(substr(date, 1, 10)) as max_date
                FROM matches WHERE home_team = ? OR away_team = ?
            ),
            DateRange AS (
                SELECT min_date as date FROM MinMaxDates WHERE min_date IS NOT NULL
                UNION ALL
                SELECT date(date, '+1 day') FROM DateRange
                WHERE date < (SELECT max_date FROM MinMaxDates)
            )
            SELECT
                dr.date,
                (SELECT avg_sentiment FROM daily_sentiment WHERE team = ? AND date = dr.date) as daily_sentiment,
                (SELECT CASE WHEN home_team = ? THEN home_rank ELSE away_rank END
                 FROM matches
                 WHERE (home_team = ? OR away_team = ?) AND substr(date, 1, 10) <= dr.date
                 ORDER BY date DESC LIMIT 1) as rank
            FROM DateRange dr
            """;

    public TeamEvolutionQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String teamName) {
        JsonArray results = new JsonArray();
        try (Connection connection = dbManager.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY)) {
            for (int parameterIndex = 1; parameterIndex <= PARAMETER_COUNT; parameterIndex++) {
                preparedStatement.setString(parameterIndex, teamName);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JsonObject dataPoint = new JsonObject();
                    dataPoint.addProperty("date", resultSet.getString("date"));
                    Double sentimentScore = resultSet.getObject("daily_sentiment", Double.class);
                    if (Objects.nonNull(sentimentScore)) {
                        dataPoint.addProperty("sentiment", sentimentScore);
                    }
                    Integer teamRank = resultSet.getObject("rank", Integer.class);
                    if (Objects.nonNull(teamRank)) {
                        dataPoint.addProperty("rank", teamRank);
                    }
                    results.add(dataPoint);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in TeamEvolutionQuery: " + e.getMessage());
        }
        return results;
    }
}