package com.resultsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        try {

            String url = "jdbc:mysql://localhost:3306/student_result_ui";
            String user = "root";
            String pass = "24B11AI388";

            return DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {

            System.out.println("Database Connection Failed!");
            return null;
        }
    }
}