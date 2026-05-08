package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ScatterQuery {
    private final DatabaseManager dbManager;

    public ScatterQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String team) {
        JsonArray results = new JsonArray();
        String sql = """
                SELECT
                CASE WHEN m.home_team = ? THEN m.home_rank ELSE m.away_rank END as rank,
                SUM(s.avg_sentiment * s.news_count) / SUM(s.news_count) as day_sentiment,
                m.date as match_date
            FROM matches m
            JOIN daily_sentiment s ON substr(m.date, 1, 10) = s.date AND s.team = ?
            WHERE m.home_team = ? OR m.away_team = ?
            GROUP BY m.date
            """;

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, team);
            pstmt.setString(2, team);
            pstmt.setString(3, team);
            pstmt.setString(4, team);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonArray point = new JsonArray();
                point.add(rs.getInt("rank"));
                point.add(rs.getDouble("day_sentiment"));
                point.add(rs.getString("match_date"));
                results.add(point);
            }
        } catch (Exception e) {
            System.err.println("Error en ScatterQuery: " + e.getMessage());
        }
        return results;
    }
}