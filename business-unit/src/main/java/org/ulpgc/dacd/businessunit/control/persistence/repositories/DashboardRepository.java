package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardRepository {
    private final DatabaseManager dbManager;

    public DashboardRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray getThermometerData(String team) {
        JsonArray results = new JsonArray();
        String sql = """
                SELECT 
                    ds.date, 
                    AVG(ds.avg_sentiment) as daily_sentiment,
                    MAX(CASE WHEN m.home_team = ? THEN m.home_rank ELSE m.away_rank END) as rank
                FROM daily_sentiment ds
                LEFT JOIN matches m ON ds.date = substr(m.date, 1, 10) AND (m.home_team = ? OR m.away_team = ?)
                WHERE ds.team = ?
                GROUP BY ds.date
                ORDER BY ds.date ASC
                """;

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, team);
            pstmt.setString(2, team);
            pstmt.setString(3, team);
            pstmt.setString(4, team);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject point = new JsonObject();
                point.addProperty("date", rs.getString("date"));
                point.addProperty("sentiment", rs.getDouble("daily_sentiment"));

                int rank = rs.getInt("rank");
                if (!rs.wasNull()) {
                    point.addProperty("rank", rank);
                }

                results.add(point);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return results;
    }
}