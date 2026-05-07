package org.ulpgc.dacd.businessunit.control.persistence;

import com.google.gson.JsonObject;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SentimentRepository {
    private final DatabaseManager dbManager;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

    public SentimentRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(JsonObject articleJson) {
        String link = articleJson.get("link").getAsString();
        String team = articleJson.get("team").getAsString();
        String source = articleJson.get("source").getAsString();
        double sentimentScore = articleJson.get("sentimentScore").getAsDouble();
        Instant pubDate = Instant.parse(articleJson.get("pubDate").getAsString());

        String dateStr = dateFormatter.format(pubDate);

        try (Connection conn = dbManager.connect()) {
            conn.setAutoCommit(false);

            if (isNew(conn, link, team)) {
                updateDailySentiment(conn, dateStr, team, source, sentimentScore);
                conn.commit();
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private boolean isNew(Connection conn, String url, String team) throws SQLException {
        String sql = "INSERT OR IGNORE INTO processed_news (url, team, inserted_at) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, url);
            pstmt.setString(2, team);
            pstmt.setString(3, Instant.now().toString());
            return pstmt.executeUpdate() > 0;
        }
    }

    private void updateDailySentiment(Connection conn, String dateStr, String team, String source, double sentimentScore) throws SQLException {
        String selectSql = "SELECT news_count, avg_sentiment FROM daily_sentiment WHERE date = ? AND team = ? AND source = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, dateStr);
            pstmt.setString(2, team);
            pstmt.setString(3, source);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int oldCount = rs.getInt("news_count");
                double oldAvg = rs.getDouble("avg_sentiment");

                int newCount = oldCount + 1;
                double newAvg = ((oldAvg * oldCount) + sentimentScore) / newCount;

                String updateSql = "UPDATE daily_sentiment SET news_count = ?, avg_sentiment = ? WHERE date = ? AND team = ? AND source = ?";
                try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                    upstmt.setInt(1, newCount);
                    upstmt.setDouble(2, newAvg);
                    upstmt.setString(3, dateStr);
                    upstmt.setString(4, team);
                    upstmt.setString(5, source);
                    upstmt.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO daily_sentiment (date, team, source, news_count, avg_sentiment) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement instmt = conn.prepareStatement(insertSql)) {
                    instmt.setString(1, dateStr);
                    instmt.setString(2, team);
                    instmt.setString(3, source);
                    instmt.setInt(4, 1);
                    instmt.setDouble(5, sentimentScore);
                    instmt.executeUpdate();
                }
            }
        }
    }
}