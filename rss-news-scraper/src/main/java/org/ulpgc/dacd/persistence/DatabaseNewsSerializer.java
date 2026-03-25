package org.ulpgc.dacd.persistence;

import org.ulpgc.dacd.model.NewsArticle;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

public class DatabaseNewsSerializer implements NewsSerializer {

    private final String url;

    public DatabaseNewsSerializer() {
        this.url = "jdbc:sqlite:database/sports_bias.db";
        initDatabase();
    }

    private void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS news (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "link TEXT NOT NULL UNIQUE," +
                "pub_date TEXT," +
                "source TEXT," +
                "team TEXT," +
                "captured_at TEXT" +
                ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void serialize(List<NewsArticle> articles) {
        String sql = "INSERT OR IGNORE INTO news (title, link, pub_date, source, team, captured_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            String now = Instant.now().toString();

            for (NewsArticle article : articles) {
                pstmt.setString(1, article.title());
                pstmt.setString(2, article.link());
                pstmt.setString(3, article.date().toString());
                pstmt.setString(4, article.source());
                pstmt.setString(5, article.team());
                pstmt.setString(6, now);

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Error al guardar las noticias: " + e.getMessage());
        }
    }
}