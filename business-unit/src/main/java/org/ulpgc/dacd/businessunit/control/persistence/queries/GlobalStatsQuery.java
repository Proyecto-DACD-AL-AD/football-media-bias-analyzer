package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.*;

public class GlobalStatsQuery {
    private final DatabaseManager dbManager;
    public static final String SQL_QUERY = """
            SELECT team, source, strftime('%w', date) as day_of_week, SUM(news_count) as daily_total
            FROM daily_sentiment
            GROUP BY team, source, day_of_week
            """;

    public GlobalStatsQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute() {
        JsonArray results = new JsonArray();

        try (Connection connection = dbManager.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL_QUERY)) {

            while (resultSet.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("team", resultSet.getString("team"));
                obj.addProperty("source", resultSet.getString("source"));
                obj.addProperty("day", resultSet.getInt("day_of_week"));
                obj.addProperty("count", resultSet.getInt("daily_total"));
                results.add(obj);
            }
        } catch (SQLException e) {
            System.err.println("Error en GlobalStatsQuery: " + e.getMessage());
        }
        return results;
    }
}