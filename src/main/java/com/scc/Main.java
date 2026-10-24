package com.scc;


import com.scc.dao.DatabaseInitializer;
import com.scc.dao.Userdao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        DatabaseInitializer.initializer();

        Scanner scanner = new Scanner(System.in);
        Userdao userDao = new Userdao();
        Authentication auth = new Authentication(userDao , scanner);
        auth.start();
        scanner.close();


    }



}




