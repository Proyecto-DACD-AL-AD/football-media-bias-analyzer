package org.ulpgc.dacd.businessunit.control.persistence;

import org.ulpgc.dacd.businessunit.control.persistence.repositories.SqlRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String dbPath;

    public DatabaseManager(String dbPath) {
        this.dbPath = "jdbc:sqlite:" + dbPath;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(dbPath);
    }

    public void initialize(java.util.List<SqlRepository> repositories) {
        repositories.forEach(SqlRepository::initTables);
    }
}