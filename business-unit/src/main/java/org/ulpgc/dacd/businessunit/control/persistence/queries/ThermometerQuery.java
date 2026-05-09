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
                TeamMatches AS (
                    SELECT substr(date, 1, 10) as match_date,
                           CASE WHEN home_team = ? THEN home_rank ELSE away_rank END as rank,
                           CASE WHEN home_goals = away_goals THEN 'DRAW'
                                WHEN (home_team = ? AND home_goals > away_goals) OR (away_team = ? AND away_goals > home_goals) THEN 'WIN'
                                ELSE 'LOSS' END as result
                    FROM matches WHERE home_team = ? OR away_team = ?
                ),
                MinMaxDates AS (
                    SELECT MIN(match_date) as min_date, MAX(match_date) as max_date FROM TeamMatches
                ),
                DateRange AS (
                    SELECT min_date as date FROM MinMaxDates WHERE min_date IS NOT NULL
                    UNION ALL
                    SELECT date(date, '+1 day') FROM DateRange
                    WHERE date < (SELECT max_date FROM MinMaxDates)
                )
                SELECT
                    dr.date,
                    ds.avg_sentiment as daily_sentiment,
                    (SELECT rank FROM TeamMatches tm WHERE tm.match_date <= dr.date ORDER BY tm.match_date DESC LIMIT 1) as rank,
                    (SELECT result FROM TeamMatches tm WHERE tm.match_date <= dr.date ORDER BY tm.match_date DESC LIMIT 1) as match_result
                FROM DateRange dr
                LEFT JOIN daily_sentiment ds ON ds.team = ? AND ds.date = dr.date
                """;

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for(int i = 1; i <= 6; i++) {
                pstmt.setString(i, team);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonObject point = new JsonObject();
                point.addProperty("date", rs.getString("date"));

                double sentiment = rs.getDouble("daily_sentiment");
                if (!rs.wasNull()) point.addProperty("sentiment", sentiment);

                int rank = rs.getInt("rank");
                if (!rs.wasNull()) point.addProperty("rank", rank);

                String matchResult = rs.getString("match_result");
                if (matchResult != null) point.addProperty("match_result", matchResult);

                results.add(point);
            }
        } catch (Exception ignored) {}
        return results;
    }
}