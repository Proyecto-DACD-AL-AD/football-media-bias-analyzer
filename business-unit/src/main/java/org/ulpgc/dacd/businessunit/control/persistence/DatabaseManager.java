package org.ulpgc.dacd.businessunit.control.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String dbPath;

    public DatabaseManager(String dbPath) {
        this.dbPath = "jdbc:sqlite:" + dbPath;
    }

    public void initDatabase() {
        String createProcessedNewsTable = """
                CREATE TABLE IF NOT EXISTS processed_news (
                    url TEXT,
                    team TEXT,
                    inserted_at TEXT,
                    PRIMARY KEY (url, team)
                );
                """;

        String createDailySentimentTable = """
                CREATE TABLE IF NOT EXISTS daily_sentiment (
                    date TEXT,
                    team TEXT,
                    source TEXT,
                    news_count INTEGER,
                    avg_sentiment REAL,
                    PRIMARY KEY (date, team, source)
                );
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createProcessedNewsTable);
            stmt.execute(createDailySentimentTable);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(dbPath);
    }
}