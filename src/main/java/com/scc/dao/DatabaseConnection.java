package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL_= "jdbc:sqlite:clothinginventoryrecords.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL_);



    }



}
