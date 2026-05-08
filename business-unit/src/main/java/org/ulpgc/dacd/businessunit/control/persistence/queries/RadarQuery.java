package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RadarQuery {
    private final DatabaseManager dbManager;

    public RadarQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String team) {
        JsonArray results = new JsonArray();

        String sql = "SELECT source, SUM(avg_sentiment * news_count) / SUM(news_count) as global_sentiment " +
                "FROM daily_sentiment " +
                "WHERE team = ? " +
                "GROUP BY source";

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, team);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject point = new JsonObject();
                point.addProperty("source", rs.getString("source"));
                point.addProperty("sentiment", rs.getDouble("global_sentiment"));
                results.add(point);
            }
        } catch (Exception e) {
            System.err.println("Error en el RadarQuery: " + e.getMessage());
        }
        return results;
    }
}