package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SourceSentimentQuery {
    private final DatabaseManager dbManager;
    private static final String SQL_QUERY = """
            SELECT source, SUM(avg_sentiment * news_count) / SUM(news_count) as global_sentiment
            FROM daily_sentiment
            WHERE team = ?
            GROUP BY source
            """;

    public SourceSentimentQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String team) {
        JsonArray results = new JsonArray();
        try (Connection conn = dbManager.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY)) {

            preparedStatement.setString(1, team);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JsonObject point = new JsonObject();
                    point.addProperty("source", resultSet.getString("source"));
                    point.addProperty("sentiment", resultSet.getDouble("global_sentiment"));
                    results.add(point);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in RadarQuery: " + e.getMessage());
        }
        return results;
    }
}