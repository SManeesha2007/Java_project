package com.resultsystem.service;

import java.sql.*;
import java.util.Scanner;

import com.resultsystem.dashboard.AdminDashboard;
import com.resultsystem.database.DBConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class ResultServiceImpl implements ResultService {

    Scanner sc = new Scanner(System.in);

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

    // ADD RESULT
    @Override
    public void addResult() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(sc.nextLine());

            // Automatically get subjects for that semester
            String[] subjects = AdminDashboard.getSubjectsBySemester(String.valueOf(sem));

            System.out.println("\nEnter marks for Semester " + sem + " subjects:\n");

            for (String sub : subjects) {

                System.out.print("Enter marks for " + sub + ": ");
                int marks = Integer.parseInt(sc.nextLine());

                String grade = AdminDashboard.calculateGrade(marks);

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO results (roll, semester, subject, marks, grade) VALUES (?,?,?,?,?)");

                ps.setString(1, roll);
                ps.setInt(2, sem);
                ps.setString(3, sub);
                ps.setInt(4, marks);
                ps.setString(5, grade);

                ps.executeUpdate();
            }

            System.out.println("\nResults added successfully!");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }


    // VIEW RESULT
    @Override
    public void viewResults() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT semester, subject, marks, grade FROM results WHERE roll=? ORDER BY semester");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            int prevSem = -1;

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

                if (sem != prevSem) {

                    // print previous semester SGPA
                    if (prevSem != -1) {

                        double sgpa = semTotalPoints / semSubjects;

                        System.out.printf("SGPA: %.2f\n", sgpa);
                        System.out.println("--------------------------------------------");

                        totalCGPA += sgpa;
                        semCount++;

                        semTotalPoints = 0;
                        semSubjects = 0;
                    }

                    System.out.println("\n========== SEMESTER " + sem + " ==========");

                    System.out.printf("%-35s %-10s %-10s\n",
                            "Subject", "Marks", "Grade");

                    System.out.println("--------------------------------------------------------------");

                    prevSem = sem;
                }

                System.out.printf("%-35s %-10d %-10s\n",
                        subject, marks, grade);

                semTotalPoints += getGradePoint(grade);
                semSubjects++;
            }

            // print last semester SGPA
            if (semSubjects > 0) {

                double sgpa = semTotalPoints / semSubjects;

                System.out.printf("SGPA: %.2f\n", sgpa);

                totalCGPA += sgpa;
                semCount++;
            }

            // print CGPA
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

    @Override
    public void updateResult() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(sc.nextLine());

            String[] subjects = AdminDashboard.getSubjectsBySemester(String.valueOf(sem));

            System.out.println("\nModify marks for Semester " + sem + "\n");

            for(String sub : subjects){

                System.out.print("Enter new marks for " + sub + ": ");
                int marks = Integer.parseInt(sc.nextLine());

                String grade = AdminDashboard.calculateGrade(marks);

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE results SET marks=?, grade=? WHERE roll=? AND semester=? AND subject=?");

                ps.setInt(1, marks);
                ps.setString(2, grade);
                ps.setString(3, roll);
                ps.setInt(4, sem);
                ps.setString(5, sub);

                ps.executeUpdate();
            }

            System.out.println("\nResults updated successfully!");

        } catch(Exception e){

            System.out.println("Error: " + e.getMessage());
        }
    }
}