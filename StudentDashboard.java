package com.resultsystem.dashboard;

import java.io.Console;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

import com.resultsystem.database.DBConnection;
import com.resultsystem.management.ResultManagement;

public class StudentDashboard {

    static Scanner sc = new Scanner(System.in);

    private static double getGradePoint(String grade) {

        return switch (grade) {
            case "A+" -> 10;
            case "A" -> 9;
            case "B+" -> 8;
            case "B" -> 7;
            case "C" -> 6;
            case "D" -> 5;
            default -> 0;
        };
    }

    public static void studentLogin(){

        try(Connection con = DBConnection.getConnection()){

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            Console console = System.console();
            String password;

            if (console != null) {

                char[] passArray = console.readPassword("Enter Password: ");
                password = new String(passArray);

            } else {

                System.out.print("Enter Password: ");
                password = sc.nextLine();
            }

            PreparedStatement pst = con.prepareStatement(
                    "SELECT * FROM students WHERE roll=? AND password=?");

            pst.setString(1,roll);
            pst.setString(2,password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()){

                System.out.println("Login Successful!");
                studentMenu(roll);

            }else{

                System.out.println("Invalid Login!");
            }

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }

    static void studentMenu(String roll){

        while(true){

            System.out.println("\n----- STUDENT DASHBOARD -----");
            System.out.println("1 View Details");
            System.out.println("2 View Results");
            System.out.println("3 View Internal Marks");
            System.out.println("4 Download Transcript");
            System.out.println("5 Apply Revaluation");
            System.out.println("6 Change Password");
            System.out.println("7 Logout");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch(ch){

                case 1 -> viewDetails(roll);
                case 2 -> viewMyResults(roll);
                case 3 -> viewInternalMarks(roll);
                case 4 -> ResultManagement.generateTranscript(roll);
                case 5 -> applyRevaluation(roll);
                case 6 -> changePassword(roll);
                case 7 -> {return;}

                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    static void viewDetails(String roll){

        try(Connection con = DBConnection.getConnection()){

            PreparedStatement pst = con.prepareStatement(
                    "SELECT * FROM students WHERE roll=?");

            pst.setString(1,roll);

            ResultSet rs = pst.executeQuery();

            if(rs.next()){

                System.out.println("\nRoll: "+rs.getString("roll"));
                System.out.println("Name: "+rs.getString("name"));
                System.out.println("Department: "+rs.getString("department"));
                System.out.println("Semester: "+rs.getInt("semester"));
            }

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }

    static void viewMyResults(String roll) {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement pst = con.prepareStatement(
                    "SELECT semester, subject, marks, grade FROM results WHERE roll=? ORDER BY semester");

            pst.setString(1, roll);

            ResultSet rs = pst.executeQuery();

            int previousSem = -1;

            double semTotalPoints = 0;
            int semSubjects = 0;

            double totalCGPA = 0;
            int semCount = 0;

            boolean found = false;

            while (rs.next()) {

                found = true;

                int sem = rs.getInt("semester");
                String subject = rs.getString("subject");
                int marks = rs.getInt("marks");
                String grade = rs.getString("grade");

                if (sem != previousSem) {

                    // Print SGPA for previous semester
                    if (previousSem != -1) {

                        double sgpa = semTotalPoints / semSubjects;

                        System.out.printf("SGPA: %.2f\n", sgpa);
                        System.out.println("-----------------------------------");

                        totalCGPA += sgpa;
                        semCount++;

                        semTotalPoints = 0;
                        semSubjects = 0;
                    }

                    System.out.println("\n========== SEMESTER " + sem + " ==========");
                    System.out.println("Subject                       Marks      Grade");
                    System.out.println("---------------------------------------------------");

                    previousSem = sem;
                }

                System.out.printf("%-30s %-10d %-10s\n",
                        subject, marks, grade);

                semTotalPoints += getGradePoint(grade);
                semSubjects++;
            }

            // Print last semester SGPA
            if (semSubjects > 0) {

                double sgpa = semTotalPoints / semSubjects;

                System.out.printf("SGPA: %.2f\n", sgpa);
                System.out.println("-----------------------------------");

                totalCGPA += sgpa;
                semCount++;
            }

            // Print final CGPA
            if (semCount > 0) {

                double cgpa = totalCGPA / semCount;

                System.out.printf("\nFINAL CGPA: %.2f\n", cgpa);
            }

            if (!found) {
                System.out.println("No results found.");
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }

    static void changePassword(String roll){

        try(Connection con = DBConnection.getConnection()){

            System.out.print("Enter New Password: ");
            String pass = sc.nextLine();

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE students SET password=? WHERE roll=?");

            pst.setString(1,pass);
            pst.setString(2,roll);

            pst.executeUpdate();

            System.out.println("Password Updated!");

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }
    static void viewInternalMarks(String roll) {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT semester, subject, mid1, mid2 FROM internal_marks WHERE roll=?");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n----------- INTERNAL MARKS -----------");

            System.out.printf("%-30s %-10s %-10s %-10s\n",
                    "Subject", "Mid1(16)", "Mid2(24)", "Total");

            System.out.println("--------------------------------------------------------------");

            while (rs.next()) {

                int mid1 = rs.getInt("mid1");
                int mid2 = rs.getInt("mid2");

                int total = mid1 + mid2;

                System.out.printf("%-30s %-10d %-10d %-10d\n",
                        rs.getString("subject"),
                        mid1,
                        mid2,
                        total);
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
    static void applyRevaluation(String roll) {

        try(Connection con = DBConnection.getConnection()){

            PreparedStatement ps = con.prepareStatement(
            "SELECT semester,subject FROM results WHERE roll=? AND grade='F'");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            ArrayList<String> failedSubjects = new ArrayList<>();

            System.out.println("\nFailed Subjects:");

            int i = 1;

            while(rs.next()){

                String sub = rs.getString("subject");
                failedSubjects.add(sub);

                System.out.println(i + ". " + sub);
                i++;
            }

            if(failedSubjects.size() == 0){

                System.out.println("No failed subjects. Revaluation not allowed.");
                return;
            }

            System.out.print("Select subject for revaluation: ");
            int choice = sc.nextInt();
            sc.nextLine();

            String subject = failedSubjects.get(choice-1);

            // 🔹 CHECK IF ALREADY APPLIED
            PreparedStatement check = con.prepareStatement(
            "SELECT * FROM revaluation WHERE roll=? AND subject=?");

            check.setString(1, roll);
            check.setString(2, subject);

            ResultSet crs = check.executeQuery();

            if(crs.next()){

                System.out.println("You have already applied for revaluation of this subject.");
                return;
            }
            System.out.println("Revaluation fee: 500");
            System.out.print("Apply revaluation for this subject? (yes/no): ");
            String confirm = sc.nextLine();

            if(confirm.equalsIgnoreCase("yes")){
                
                PreparedStatement pst = con.prepareStatement(
                "INSERT INTO revaluation VALUES(?,?,?, 'Applied',500)");

                pst.setString(1, roll);
                pst.setString(2, subject);
                pst.setInt(3, 0);

                pst.executeUpdate();

                System.out.println("Revaluation request submitted successfully.");
            }else{
                System.out.println("Request cancelled.");
            }
            }catch(Exception e){

                System.out.println(e.getMessage());
            }
        }
}