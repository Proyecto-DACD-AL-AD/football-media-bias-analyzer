import java.sql.*;
import java.util.List;

public class FootballDatabaseManager {

    private static final String DB_PATH = "database/sports_bias.db";

    private Connection connect() throws SQLException {
        String databaseUrl = "jdbc:sqlite:" + DB_PATH;
        return DriverManager.getConnection(databaseUrl);
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
                "status TEXT,\n" +
                "match_date TEXT,\n" +
                "captured_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(sqlCreate);
            System.out.println("Base de datos conectada y tabla 'matches' preparada.");

        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    public void insertMatches(List<MatchResponse> matches) {

        String sqlInsert = "INSERT INTO matches (matchday, home_team, away_team, home_goals, away_goals, " +
                "home_rank_after_matchday, away_rank_after_matchday, status, match_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {

            for (MatchResponse match : matches) {
                preparedStatement.setInt(1, match.getMatchday());
                preparedStatement.setString(2, match.getHomeTeam().getName());
                preparedStatement.setString(3, match.getAwayTeam().getName());
                preparedStatement.setInt(4, match.getScore().getFullTime().getHomeGoals());
                preparedStatement.setInt(5, match.getScore().getFullTime().getAwayGoals());
                preparedStatement.setInt(6, match.getHomeRankAfterMatchday());
                preparedStatement.setInt(7, match.getAwayRankAfterMatchday());
                preparedStatement.setString(8, match.getStatus());
                preparedStatement.setString(9, match.getDate());

                preparedStatement.executeUpdate();
            }
            System.out.println("Partidos insertados correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al insertar partidos: " + e.getMessage());
        }
    }
}