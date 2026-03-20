import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}