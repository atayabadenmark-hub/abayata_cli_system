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

    private final ActivityLogDao logDao = new ActivityLogDao();

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
        int newUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, username);
            pstm.setString(2, password);
            pstm.setString(3, roles.toUpperCase());
            pstm.setString(4, "PENDING");
            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    newUserId = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }

        if (newUserId != -1) {
            logDao.logAction(newUserId, "USER_REGISTERED", "User registered with role: " + roles.toUpperCase());
        }

        System.out.println("User registered successfully in the database!");
        return true;
    }

    public boolean addUserByAdmin(int adminId, String username, String password, String roles, String status) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, ?)";
        int newUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, username);
            pstm.setString(2, password);
            pstm.setString(3, roles.toUpperCase());
            pstm.setString(4, status.toUpperCase());
            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    newUserId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("FAILED to add user: " + e.getMessage());
            return false;
        }

        if (newUserId != -1) {
            logDao.logAction(adminId, "USER_ADDED", "Admin ID " + adminId + " created User ID " + newUserId + " (" + username + ")");
        }

        return true;
    }

    public user login(String username, String password) {
        String sql = "SELECT user_id, username, password, roles, status FROM users WHERE username = ? AND password = ?";
        user loggedUser = null;
        int pendingUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    int userId = rs.getInt("user_id");

                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for Approval");
                        pendingUserId = userId;
                    } else {
                        loggedUser = new user(
                                userId,
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("roles"),
                                status
                        );
                    }
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }

        if (pendingUserId != -1) {
            logDao.logAction(pendingUserId, "LOGIN_FAILED", "User " + username + " attempted login while status was PENDING");
        } else if (loggedUser != null) {
            logDao.logAction(loggedUser.user_id(), "USER_LOGIN", "User " + username + " (ID: " + loggedUser.user_id() + ") logged in successfully.");
        }

        return loggedUser;
    }

    public List<user> getAllUser() {
        List<user> usersList = new ArrayList<>();
        String sql = "SELECT user_id, username, password, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new user(
                        rs.getInt("user_id"),
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
        String sql = "SELECT user_id, username, password, roles, status FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new user(
                        rs.getInt("user_id"),
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

    public boolean approveUser(int adminId, int targetUserId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetUserId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }

        if (success) {
            logDao.logAction(adminId, "USER_APPROVED", "Admin ID " + adminId + " approved account for User ID " + targetUserId);
        }
        return success;
    }

    public boolean updateUserRoleAndStatus(int adminId, int targetUserId, String newRole, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE user_id = ?";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, targetUserId);

            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }

        if (success) {
            logDao.logAction(adminId, "USER_UPDATED", "Admin ID " + adminId + " changed role of User " + targetUserId + " to " + newRole.toUpperCase() + " with status " + newStatus.toUpperCase());
        }
        return success;
    }

    public boolean deleteUser(int adminId, int targetUserId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetUserId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }

        if (success) {
            logDao.logAction(adminId, "USER_DELETED", "Admin ID " + adminId + " deleted User ID " + targetUserId);
        }
        return success;
    }
}