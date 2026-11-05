package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializer() {
        String enableWalSQL = "PRAGMA journal_mode = WAL;";

        // SQL query to create the 'users' table
        String createUsersTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;

        // SQL query to create the 'activity_logs' table with Philippine Time default (+8 hours)
        String createActivityLogsTableSQL = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp DATETIME DEFAULT (datetime('now', '+8 hours')),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable WAL mode to prevent locking issues
            stmt.execute(enableWalSQL);

            stmt.execute(createUsersTableSQL);
            stmt.execute(createActivityLogsTableSQL);

            System.out.println("Database tables successfully initialized (WAL mode active).");

        } catch (SQLException e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }
}