package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonObject;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MatchesRepository implements EventRepository {
    private final DatabaseManager dbManager;

    public MatchesRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void initTables() {
        String createMatchesTable = """
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

        try (Connection conn = dbManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createMatchesTable);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void save(JsonObject json) {
        String date = json.get("date").getAsString();
        int matchday = json.get("matchday").getAsInt();
        String homeTeam = json.get("homeTeam").getAsString();
        String awayTeam = json.get("awayTeam").getAsString();
        int homeGoals = json.get("homeGoals").getAsInt();
        int awayGoals = json.get("awayGoals").getAsInt();
        int homeRank = json.get("homeRankAfterMatchday").getAsInt();
        int awayRank = json.get("awayRankAfterMatchday").getAsInt();

        String sql = "INSERT OR IGNORE INTO matches (date, matchday, home_team, away_team, home_goals, away_goals, home_rank, away_rank) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, matchday);
            pstmt.setString(3, homeTeam);
            pstmt.setString(4, awayTeam);
            pstmt.setInt(5, homeGoals);
            pstmt.setInt(6, awayGoals);
            pstmt.setInt(7, homeRank);
            pstmt.setInt(8, awayRank);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}