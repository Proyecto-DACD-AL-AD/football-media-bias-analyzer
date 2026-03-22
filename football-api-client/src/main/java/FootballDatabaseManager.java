import java.sql.*;
import java.util.List;

public class FootballDatabaseManager {

    private static final String DB_PATH = "database/sports_bias.db";

    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + DB_PATH;
        return DriverManager.getConnection(url);
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS matches (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "home_team TEXT NOT NULL,\n" +
                "away_team TEXT NOT NULL,\n" +
                "home_goals INTEGER,\n" +
                "away_goals INTEGER,\n" +
                "status TEXT,\n" +
                "match_date TEXT,\n" +
                "captured_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);
            System.out.println("Base de datos conectada y tabla 'matches' preparada.");

        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    public void insertMatches(List<MatchResponse> matches) {

        String sql = "INSERT INTO matches (home_team, away_team, home_goals, away_goals, status, match_date) " +
                "VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (MatchResponse match : matches) {
                preparedStatement.setString(1, match.getHomeTeam().getName());
                preparedStatement.setString(2, match.getAwayTeam().getName());
                preparedStatement.setInt(3, match.getScore().getFullTime().getHomeGoals());
                preparedStatement.setInt(4, match.getScore().getFullTime().getAwayGoals());
                preparedStatement.setString(5, match.getStatus());
                preparedStatement.setString(6, match.getDate());

                preparedStatement.executeUpdate();
            }
            System.out.println("Partidos insertados correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al insertar partidos: " + e.getMessage());
        }
    }
}