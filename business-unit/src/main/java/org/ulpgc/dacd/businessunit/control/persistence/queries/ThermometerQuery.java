package org.ulpgc.dacd.businessunit.control.persistence.queries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThermometerQuery {
    private final DatabaseManager dbManager;

    public ThermometerQuery(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public JsonArray execute(String team) {
        JsonArray results = new JsonArray();
        String sql = """
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

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, team);
            pstmt.setString(2, team);
            pstmt.setString(3, team);
            pstmt.setString(4, team);
            pstmt.setString(5, team);
            pstmt.setString(6, team);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonObject point = new JsonObject();
                point.addProperty("date", rs.getString("date"));
                
                double sentiment = rs.getDouble("daily_sentiment");
                if (!rs.wasNull()) point.addProperty("sentiment", sentiment);
                
                int rank = rs.getInt("rank");
                if (!rs.wasNull()) point.addProperty("rank", rank);
                
                results.add(point);
            }
        } catch (Exception ignored) {}
        return results;
    }
}