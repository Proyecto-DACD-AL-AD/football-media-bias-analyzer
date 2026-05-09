package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NewsRepository implements SqlRepository {
    private final DatabaseManager dbManager;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.of("UTC"));

    private static final String CREATE_TABLE_PROCESSED_NEWS = """
            CREATE TABLE IF NOT EXISTS processed_news (
                url TEXT,
                team TEXT,
                inserted_at TEXT,
                PRIMARY KEY (url, team)
            );
            """;

    private static final String CREATE_TABLE_DAILY_SENTIMENT = """
            CREATE TABLE IF NOT EXISTS daily_sentiment (
                date TEXT,
                team TEXT,
                source TEXT,
                news_count INTEGER,
                avg_sentiment REAL,
                PRIMARY KEY (date, team, source)
            );
            """;

    private static final String INSERT_PROCESSED_SQL = "INSERT OR IGNORE INTO processed_news (url, team, inserted_at) VALUES (?, ?, ?)";
    private static final String SELECT_SENTIMENT_SQL = "SELECT news_count, avg_sentiment FROM daily_sentiment WHERE date = ? AND team = ? AND source = ?";
    private static final String UPDATE_SENTIMENT_SQL = "UPDATE daily_sentiment SET news_count = ?, avg_sentiment = ? WHERE date = ? AND team = ? AND source = ?";
    private static final String INSERT_SENTIMENT_SQL = "INSERT INTO daily_sentiment (date, team, source, news_count, avg_sentiment) VALUES (?, ?, ?, ?, ?)";

    public NewsRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void initTables() {
        try (Connection conn = dbManager.connect();
             Statement statement = conn.createStatement()) {
            statement.execute(CREATE_TABLE_PROCESSED_NEWS);
            statement.execute(CREATE_TABLE_DAILY_SENTIMENT);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void save(JsonObject articleJson) {
        String link = articleJson.get("link").getAsString();
        String team = articleJson.get("team").getAsString();
        String source = articleJson.get("source").getAsString();
        double sentimentScore = articleJson.get("sentimentScore").getAsDouble();
        String dateStr = dateFormatter.format(Instant.parse(articleJson.get("pubDate").getAsString()));

        try (Connection conn = dbManager.connect()) {
            conn.setAutoCommit(false);
            try {
                if (isNew(conn, link, team)) {
                    updateDailySentiment(conn, dateStr, team, source, sentimentScore);
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private boolean isNew(Connection conn, String url, String team) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_PROCESSED_SQL)) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, team);
            preparedStatement.setString(3, Instant.now().toString());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private void updateDailySentiment(Connection conn, String dateStr, String team, String source, double sentimentScore) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_SENTIMENT_SQL)) {
            preparedStatement.setString(1, dateStr);
            preparedStatement.setString(2, team);
            preparedStatement.setString(3, source);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int oldCount = resultSet.getInt("news_count");
                    double oldAvg = resultSet.getDouble("avg_sentiment");
                    int newCount = oldCount + 1;
                    double newAvg = ((oldAvg * oldCount) + sentimentScore) / newCount;
                    updateEntry(conn, dateStr, team, source, newCount, newAvg);
                } else {
                    insertEntry(conn, dateStr, team, source, sentimentScore);
                }
            }
        }
    }

    private void updateEntry(Connection conn, String date, String team, String source, int count, double avg) throws SQLException {
        try (PreparedStatement updatePreparedStatement = conn.prepareStatement(UPDATE_SENTIMENT_SQL)) {
            updatePreparedStatement.setInt(1, count);
            updatePreparedStatement.setDouble(2, avg);
            updatePreparedStatement.setString(3, date);
            updatePreparedStatement.setString(4, team);
            updatePreparedStatement.setString(5, source);
            updatePreparedStatement.executeUpdate();
        }
    }

    private void insertEntry(Connection conn, String date, String team, String source, double score) throws SQLException {
        try (PreparedStatement insertPreparedStatement = conn.prepareStatement(INSERT_SENTIMENT_SQL)) {
            insertPreparedStatement.setString(1, date);
            insertPreparedStatement.setString(2, team);
            insertPreparedStatement.setString(3, source);
            insertPreparedStatement.setInt(4, 1);
            insertPreparedStatement.setDouble(5, score);
            insertPreparedStatement.executeUpdate();
        }
    }
}