package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.ActivityLog;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.logDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    public void display(user admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("          ADMIN DASHBOARD        ");
            System.out.println("=================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User identity / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "4" -> addNewUser(admin);
                case "5" -> updateUser(admin);
                case "6" -> deleteUser(admin);
                case "7" -> viewAuditLogsMenu();
                case "8" -> {
                    System.out.println("Logging out from Admin Dashboard...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 8.");
            }
        }
    }

    private void displayUserTable(List<user> users, String title) {
        System.out.println("\n=========================" + title + "=========================");
        if (users.isEmpty()) {
            System.out.println("-> No Records Found");
            return;
        }
        System.out.println("+----+-----------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+----+-----------------+------------+------------+");
        for (user u : users) {
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.user_id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+----+-----------------+------------+------------+");
    }

    private void viewAllUser() {
        displayUserTable(userdao.getAllUser(), "ALL REGISTERED USERS");
    }

    private void viewPendingUsers() {
        displayUserTable(userdao.getPendingUsers(), "PENDING USER APPROVALS");
    }

    private void approveUser(user admin) {
        List<user> pending = userdao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("-> No pending users to approve.");
            return;
        }
        displayUserTable(pending, "PENDING USERS");
        int userId = readIntInput("Enter User ID to approve (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println("-> Operation canceled by the user.");
            return;
        }

        boolean exists = pending.stream().anyMatch(u -> u.user_id() == userId);
        if (!exists) {
            System.out.println("-> User ID not found in pending list.");
            return;
        }

        if (userdao.approveUser(admin.user_id(), userId)) {
            System.out.println("-> User ID " + userId + " approved successfully!");
        } else {
            System.out.println("-> Failed to approve user.");
        }
    }

    private void addNewUser(user admin) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        String role = readValidRole();
        if (role == null) return;
        String status = readValidStatus();
        if (status == null) return;

        if (userdao.addUserByAdmin(admin.user_id(), username, password, role, status)) {
            System.out.println("-> New user added successfully!");
        } else {
            System.out.println("-> Failed to add user.");
        }
    }

    private void updateUser(user admin) {
        List<user> users = userdao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) return;

        int userId = readIntInput("Enter User ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        if (userId == admin.user_id()) {
            System.out.println("-> Warning: Modifying current active admin is restricted.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.user_id() == userId);
        if (!exists) {
            System.out.println("-> User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) return;

        String newStatus = readValidStatus();
        if (newStatus == null) return;

        if (userdao.updateUserRoleAndStatus(admin.user_id(), userId, newRole, newStatus)) {
            System.out.println("-> User updated successfully!");
        } else {
            System.out.println("-> Failed to update user.");
        }
    }

    private void deleteUser(user admin) {
        viewAllUser();
        int userId = readIntInput("Enter User ID to delete (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("-> Deletion cancelled.");
            return;
        }

        System.out.print("Are you sure you want to delete user ID " + userId + "? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            if (userdao.deleteUser(admin.user_id(), userId)) {
                System.out.println("-> User deleted successfully!");
            } else {
                System.out.println("-> Failed to delete user.");
            }
        } else {
            System.out.println("-> Deletion cancelled.");
        }
    }


    private void viewAuditLogsMenu() {
        boolean inLogMenu = true;
        while (inLogMenu) {
            System.out.println("\n=================================");
            System.out.println("       SYSTEM AUDIT LOGS         ");
            System.out.println("=================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Return to Main Dashboard");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayLogsTable(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case "2" -> {
                    int searchUserId = readIntInput("Enter User ID to filter logs (or 0 to cancel): ");
                    if (searchUserId != 0) {
                        displayLogsTable(logDao.getLogsByUserId(searchUserId), "LOGS FOR USER ID: " + searchUserId);
                    }
                }
                case "3" -> {
                    System.out.print("Are you sure you want to clear ALL historical logs? (y/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        if (logDao.clearAllLogs()) {
                            System.out.println("-> All activity logs cleared successfully!");
                        }
                    }
                }
                case "4" -> inLogMenu = false;
                default -> System.out.println("-> Invalid option selected.");
            }
        }
    }

    private void displayLogsTable(List<ActivityLog> logs, String title) {
        System.out.println("\n=========================" + title + "=========================");
        if (logs.isEmpty()) {
            System.out.println("-> No Activity Log Records Found");
            return;
        }
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
        System.out.printf("| %-5s | %-8s | %-18s | %-35s | %-20s |\n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
        for (ActivityLog log : logs) {
            System.out.printf("| %-5d | %-8d | %-18s | %-35s | %-20s |\n",
                    log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
        }
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
    }


    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("-> [ERROR] Invalid numeric format. Please enter a valid number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter role (User / Staff / Admin) [0 or 'cancel' to abort]: ");
            String role = scanner.nextLine().trim();

            if (role.equals("0") || role.equalsIgnoreCase("cancel")) {
                return null;
            }

            String upperRole = role.toUpperCase();
            if (upperRole.equals("USER") || upperRole.equals("STAFF") || upperRole.equals("ADMIN") || upperRole.equals("ADMINISTRATOR")) {
                return upperRole;
            }
            System.out.println("-> [ERROR] Invalid role. Please choose from User, Staff, or Admin.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter status (APPROVED / PENDING) [0 or 'cancel' to abort]: ");
            String status = scanner.nextLine().trim();

            if (status.equals("0") || status.equalsIgnoreCase("cancel")) {
                return null;
            }

            String upperStatus = status.toUpperCase();
            if (upperStatus.equals("APPROVED") || upperStatus.equals("PENDING")) {
                return upperStatus;
            }
            System.out.println("-> [ERROR] Invalid status. Please enter APPROVED or PENDING.");
        }
    }
}