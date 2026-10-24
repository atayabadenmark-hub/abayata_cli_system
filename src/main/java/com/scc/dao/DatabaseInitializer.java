package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializer() {
        // Query to create the 'users' table with all required columns including status
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Database successfully initialized.");

        } catch (SQLException e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }
}