package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
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
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUser();
                case "6" -> deleteUser();
                case "7" -> {
                    System.out.println("Logging out from Admin Dashboard...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 7.");
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

    private void approveUser() {
        List<user> pending = userdao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("-> No pending users to approve.");
            return;
        }
        displayUserTable(pending, "PENDING USERS");
        System.out.print("Enter User ID to approve (or 0 to cancel): ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());

            if (userId == 0) {
                System.out.println("-> Operation canceled by the user.");
                return;
            }

            boolean exists = pending.stream().anyMatch(u -> u.user_id() == userId);
            if (!exists) {
                System.out.println("-> User ID not found in pending list.");
                return;
            }

            if (userdao.updateStatus(userId, "APPROVED")) {
                System.out.println("-> User ID " + userId + " approved successfully!");
            } else {
                System.out.println("-> Failed to approve user.");
            }
        } catch (NumberFormatException e) {
            System.out.println("-> Invalid ID format.");
        }
    }

    private void addNewUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter role (User/Staff/Admin): ");
        String role = scanner.nextLine().trim();
        System.out.print("Enter status (APPROVED/PENDING): ");
        String status = scanner.nextLine().trim();

        if (userdao.addUserByAdmin(username, password, role, status)) {
            System.out.println("-> New user added successfully!");
        } else {
            System.out.println("-> Failed to add user.");
        }
    }

    private void updateUser() {
        viewAllUser();
        System.out.print("Enter User ID to update: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter new username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter new password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Enter new role: ");
            String role = scanner.nextLine().trim();
            System.out.print("Enter new status: ");
            String status = scanner.nextLine().trim();

            if (userdao.updateUser(userId, username, password, role, status)) {
                System.out.println("-> User updated successfully!");
            } else {
                System.out.println("-> Failed to update user.");
            }
        } catch (NumberFormatException e) {
            System.out.println("-> Invalid ID format.");
        }
    }

    private void deleteUser() {
        viewAllUser();
        System.out.print("Enter User ID to delete: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Are you sure you want to delete user ID " + userId + "? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("y")) {
                if (userdao.deleteUser(userId)) {
                    System.out.println("-> User deleted successfully!");
                } else {
                    System.out.println("-> Failed to delete user.");
                }
            } else {
                System.out.println("-> Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("-> Invalid ID format.");
        }
    }
}