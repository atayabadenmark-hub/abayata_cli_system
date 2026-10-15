package com.scc;


import com.scc.dao.Userdao;
import com.scc.model.user;
import com.scc.ui.AccountManageMenu;

import java.util.Scanner;

public class Main {


    public static final Scanner sc = new Scanner(System.in);
    private static final Userdao userDao = new Userdao();
    public static void main(String[] args){

        System.out.println("\n================================");
        System.out.println("  Clothing Store Inventory System  ");
        System.out.println("================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                System.out.print("Enter new username:");
                String user = sc.nextLine();
                System.out.print("Enter Password:");
                String password = sc.nextLine();

                System.out.print("Select a roles  [1]Admin / Store Owner [2] Inventory Staff [3]Casher : ");
                int rolesChoice = sc.nextInt();
                sc.nextLine();
                String roles;

                switch (rolesChoice) {
                    case 1:
                        roles = "Admin / Store Owner";
                        break;
                    case 2:
                        roles = " Inventory Staff";
                        break;
                    case 3:
                        roles = "Casher";
                        break;

                    default:
                        roles = null;
                }

                if (roles != null) {
                    userDao.registerUser(user, password, roles);


                } else {
                    System.out.println("Invalid roles selection!");
                }

        }

    }

    private static void handleLogin() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        user user = userDao.login(username, password);

        if (user != null) {
            System.out.println("\n -> Welcome " + user.username() + " | roles: " + user.roles());

            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new AccountManageMenu(userDao, sc).display(user);
                default -> System.out.print("Account identity menu not found!");
            }
        }
    }



}




