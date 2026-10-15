package com.scc.ui;
import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.Scanner;

public class AccountManageMenu {


    private final Userdao userdao;
    private final Scanner scanner;


    public AccountManageMenu(Userdao Userdao , Scanner scanner) {
        this.userdao = Userdao;
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

        }
    }
}


