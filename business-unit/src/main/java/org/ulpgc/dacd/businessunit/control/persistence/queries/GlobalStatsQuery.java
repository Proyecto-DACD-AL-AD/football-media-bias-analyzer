package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.*;

public class GlobalStatsQuery {
    private final DatabaseManager dbManager;

    public GlobalStatsQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute() {
        JsonArray results = new JsonArray();
        String sql = """
            SELECT team, strftime('%w', date) as day_of_week, SUM(news_count) as daily_total
            FROM daily_sentiment
            GROUP BY team, day_of_week
            """;

        try (Connection conn = dbManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("team", rs.getString("team"));
                obj.addProperty("day", rs.getInt("day_of_week"));
                obj.addProperty("count", rs.getInt("daily_total"));
                results.add(obj);
            }
        } catch (SQLException e) {
            System.err.println("Error en GlobalStatsQuery: " + e.getMessage());
        }
        return results;
    }
}