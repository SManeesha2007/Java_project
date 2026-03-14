package com.resultsystem.management;

import java.sql.*;
import java.util.Scanner;

import com.resultsystem.database.DBConnection;
import com.resultsystem.dashboard.AdminDashboard;

public class InternalManagement {

    static Scanner sc = new Scanner(System.in);

   public static void menu() {

        while (true) {

            System.out.println("\n------ INTERNAL MARKS MANAGEMENT ------");
            System.out.println("1 Enter Internal Marks");
            System.out.println("2 View Internal Marks (Student)");
            System.out.println("3 Back");

            System.out.print("Enter choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {

                case 1 -> enterInternalMarks();

                case 2 -> viewInternalMarks();

                case 3 -> { return; }

                default -> System.out.println("Invalid choice");
            }
        }
    }

    static void enterInternalMarks() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT semester FROM students WHERE roll=?");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Student not found!");
                return;
            }

            int sem = rs.getInt("semester");

            String[] subjects =
                    AdminDashboard.getSubjectsBySemester(String.valueOf(sem));

            for (String sub : subjects) {

                System.out.println("\nSubject: " + sub);

                System.out.print("Enter Mid1 Marks (out of 16): ");
                int mid1 = sc.nextInt();

                System.out.print("Enter Mid2 Marks (out of 24): ");
                int mid2 = sc.nextInt();
                sc.nextLine();

                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO internal_marks VALUES(?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE mid1=?, mid2=?");

                pst.setString(1, roll);
                pst.setInt(2, sem);
                pst.setString(3, sub);
                pst.setInt(4, mid1);
                pst.setInt(5, mid2);
                pst.setInt(6, mid1);
                pst.setInt(7, mid2);

                pst.executeUpdate();
            }

            System.out.println("\nInternal marks saved successfully!");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewInternalMarks() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT semester, subject, mid1, mid2 FROM internal_marks WHERE roll=?");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("\n----------- INTERNAL MARKS -----------");

            System.out.printf("%-30s %-10s %-10s %-10s\n",
                    "Subject", "Mid1(16)", "Mid2(24)", "Total");

            System.out.println("--------------------------------------------------------------");

            while (rs.next()) {

                found = true;

                int mid1 = rs.getInt("mid1");
                int mid2 = rs.getInt("mid2");

                int total = mid1 + mid2;

                System.out.printf("%-30s %-10d %-10d %-10d\n",
                        rs.getString("subject"),
                        mid1,
                        mid2,
                        total);
            }

            if (!found) {
                System.out.println("No internal marks found for this student.");
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}