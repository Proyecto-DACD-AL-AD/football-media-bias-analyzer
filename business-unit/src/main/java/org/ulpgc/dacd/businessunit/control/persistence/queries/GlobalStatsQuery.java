package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.*;

public class GlobalStatsQuery {
    private final DatabaseManager dbManager;
    private static final String SQL = """
            SELECT team, strftime('%w', date) as day_of_week, SUM(news_count) as daily_total
            FROM daily_sentiment
            GROUP BY team, day_of_week
            """;

    public GlobalStatsQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute() {
        JsonArray results = new JsonArray();
        try (Connection conn = dbManager.connect();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL)) {

            while (resultSet.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("team", resultSet.getString("team"));
                obj.addProperty("day", resultSet.getInt("day_of_week"));
                obj.addProperty("count", resultSet.getInt("daily_total"));
                results.add(obj);
            }
        } catch (SQLException e) {
            System.err.println("Error in GlobalStatsQuery: " + e.getMessage());
        }
        return results;
    }
}