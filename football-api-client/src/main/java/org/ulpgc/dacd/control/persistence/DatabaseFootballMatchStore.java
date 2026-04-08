package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.Match;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class DatabaseFootballMatchStore implements FootballMatchStore {

    private final String url;

    public DatabaseFootballMatchStore() {
        this.url = loadUrl();
    }

    private String loadUrl() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
                return "jdbc:sqlite:" + prop.getProperty("db.path");
            }
        } catch (IOException e) {
            System.err.println("Error cargando la URL: " + e.getMessage());
        }

        return "";
    }


    private Connection connect() throws SQLException {

        return DriverManager.getConnection(url);
    }

    public void createTable() {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS matches (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "matchday INTEGER,\n" +
                "home_team TEXT NOT NULL,\n" +
                "away_team TEXT NOT NULL,\n" +
                "home_goals INTEGER,\n" +
                "away_goals INTEGER,\n" +
                "home_rank_after_matchday INTEGER,\n" +
                "away_rank_after_matchday INTEGER,\n" +
                "match_date TEXT,\n" +
                "captured_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(matchday, home_team, away_team)" +
                ");";

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(sqlCreate);
            System.out.println("Base de datos conectada y tabla 'matches' preparada.");

        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }


    public void insertMatches(List<Match> matches) {

        String sqlInsert = "INSERT OR IGNORE INTO matches (matchday, home_team, away_team, home_goals, away_goals, " +
                "home_rank_after_matchday, away_rank_after_matchday, match_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {

            for (Match match : matches) {
                preparedStatement.setInt(1, match.matchday());
                preparedStatement.setString(2, match.homeTeam());
                preparedStatement.setString(3, match.awayTeam());
                preparedStatement.setInt(4, match.homeGoals());
                preparedStatement.setInt(5, match.awayGoals());
                preparedStatement.setInt(6, match.homeRankAfterMatchday());
                preparedStatement.setInt(7, match.awayRankAfterMatchday());
                preparedStatement.setString(8, String.valueOf(match.date()));

                preparedStatement.executeUpdate();
            }
            System.out.println("Partidos insertados correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al insertar partidos: " + e.getMessage());
        }
    }
}