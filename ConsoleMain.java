package com.resultsystem.main;

import java.util.Scanner;
import com.resultsystem.dashboard.AdminDashboard;
import com.resultsystem.dashboard.StudentDashboard;

public class ConsoleMain {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== STUDENT RESULT MANAGEMENT SYSTEM =====");
            System.out.println("1. Admin Login");
            System.out.println("2. Student Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1 -> AdminDashboard.adminLogin();
                case 2 -> StudentDashboard.studentLogin();
                case 3 -> System.exit(0);
                default -> System.out.println("Invalid Choice!");
            }
        }
    }
}