package com.scc.dao;

import com.scc.model.user;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Userdao {

    public Userdao() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE roles = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, password, roles, status) VALUES('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("Default admin account created.");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String roles) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);
            pstm.setString(2, password);
            pstm.setString(3, roles.toUpperCase());
            pstm.setString(4, "PENDING");
            pstm.executeUpdate();

            System.out.println("User registered successfully in the database!");
            return true;

        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserByAdmin(String username, String password, String roles, String status) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);
            pstm.setString(2, password);
            pstm.setString(3, roles.toUpperCase());
            pstm.setString(4, status.toUpperCase());
            pstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("FAILED to add user: " + e.getMessage());
            return false;
        }
    }

    public user login(String username, String password) {
        // Changed 'id' to 'user_id'
        String sql = "SELECT user_id, username, password, roles, status FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for Approval");
                        return null;
                    }
                    return new user(
                            rs.getInt("user_id"), // Fixed to user_id
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    public List<user> getAllUser() {
        List<user> usersList = new ArrayList<>();
        // Changed 'id' to 'user_id'
        String sql = "SELECT user_id, username, password, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new user(
                        rs.getInt("user_id"), // Fixed to user_id
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }

        return usersList;
    }

    public List<user> getPendingUsers() {
        List<user> usersList = new ArrayList<>();
        // Changed 'id' to 'user_id'
        String sql = "SELECT user_id, username, password, roles, status FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new user(
                        rs.getInt("user_id"), // Fixed to user_id
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending users: " + e.getMessage());
        }

        return usersList;
    }

    public boolean updateStatus(int userId, String status) {
        // Changed 'id' to 'user_id' in WHERE clause
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.toUpperCase());
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(int userId, String username, String password, String roles, String status) {
        // Changed 'id' to 'user_id' in WHERE clause
        String sql = "UPDATE users SET username = ?, password = ?, roles = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, roles.toUpperCase());
            pstmt.setString(4, status.toUpperCase());
            pstmt.setInt(5, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        // Changed 'id' to 'user_id' in WHERE clause
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}