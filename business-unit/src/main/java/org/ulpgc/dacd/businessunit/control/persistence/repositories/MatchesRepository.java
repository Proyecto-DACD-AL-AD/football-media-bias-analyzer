package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.*;

public class MatchesRepository implements SqlRepository {
    private final DatabaseManager dbManager;

    private static final String TABLE_MATCHES = """
            CREATE TABLE IF NOT EXISTS matches (
                date TEXT,
                matchday INTEGER,
                home_team TEXT,
                away_team TEXT,
                home_goals INTEGER,
                away_goals INTEGER,
                home_rank INTEGER,
                away_rank INTEGER,
                PRIMARY KEY (date, home_team, away_team)
            );
            """;

    private static final String INSERT_MATCH_SQL = """
            INSERT OR IGNORE INTO matches
            (date, matchday, home_team, away_team, home_goals, away_goals, home_rank, away_rank)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public MatchesRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void initTables() {
        try (Connection conn = dbManager.connect();
             Statement statement = conn.createStatement()) {
            statement.execute(TABLE_MATCHES);
        } catch (SQLException e) {
            System.err.println("Error initializing Matches table: " + e.getMessage());
        }
    }

    @Override
    public void save(JsonObject matchJson) {
        try (Connection connection = dbManager.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MATCH_SQL)) {
            preparedStatement.setString(1, matchJson.get("date").getAsString());
            preparedStatement.setInt(2, matchJson.get("matchday").getAsInt());
            preparedStatement.setString(3, matchJson.get("homeTeam").getAsString());
            preparedStatement.setString(4, matchJson.get("awayTeam").getAsString());
            preparedStatement.setInt(5, matchJson.get("homeGoals").getAsInt());
            preparedStatement.setInt(6, matchJson.get("awayGoals").getAsInt());
            preparedStatement.setInt(7, matchJson.get("homeRankAfterMatchday").getAsInt());
            preparedStatement.setInt(8, matchJson.get("awayRankAfterMatchday").getAsInt());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving match: " + e.getMessage());
        }
    }
}