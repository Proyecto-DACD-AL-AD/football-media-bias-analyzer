package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankSentimentCorrelationQuery {
    private final DatabaseManager dbManager;
    private static final String SQL_QUERY = """
            SELECT
                CASE WHEN m.home_team = ? THEN m.home_rank ELSE m.away_rank END as rank,
                SUM(s.avg_sentiment * s.news_count) / SUM(s.news_count) as day_sentiment,
                m.date as match_date
            FROM matches m
            JOIN daily_sentiment s ON substr(m.date, 1, 10) = s.date AND s.team = ?
            WHERE m.home_team = ? OR m.away_team = ?
            GROUP BY m.date
            """;

    public RankSentimentCorrelationQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String teamName) {
        JsonArray results = new JsonArray();
        try (Connection connection = dbManager.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY)) {

            preparedStatement.setString(1, teamName);
            preparedStatement.setString(2, teamName);
            preparedStatement.setString(3, teamName);
            preparedStatement.setString(4, teamName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JsonArray point = new JsonArray();
                    point.add(resultSet.getInt("rank"));
                    point.add(resultSet.getDouble("day_sentiment"));
                    point.add(resultSet.getString("match_date"));
                    results.add(point);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in SentimentCorrelationQuery: " + e.getMessage());
        }
        return results;
    }
}