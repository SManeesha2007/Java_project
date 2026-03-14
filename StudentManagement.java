package com.resultsystem.management;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

import com.resultsystem.database.DBConnection;
import com.resultsystem.dashboard.AdminDashboard;

public class StudentManagement {

    static Scanner sc = new Scanner(System.in);

    public static void menu(){

        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println("1 Add Student");
        System.out.println("2 Delete Student");
        System.out.println("3 Update Student");
        System.out.println("4 Back");
        System.out.print("Enter choice: ");
        
        int ch = sc.nextInt();
        sc.nextLine();

        switch(ch){

            case 1 -> addStudent();
            case 2 -> deleteStudent();
            case 3 -> updateStudent();
            case 4 -> {return;}
        }
    }

    static void addStudent() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll: ");
            String roll = sc.nextLine();

            System.out.print("Enter Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Department: ");
            String dept = sc.nextLine();

            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(sc.nextLine());

            // Insert student
            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO students VALUES(?,?,?,?,?)");

            pst.setString(1, roll);
            pst.setString(2, name);
            pst.setString(3, dept);
            pst.setInt(4, sem);
            pst.setString(5, "1234");

            pst.executeUpdate();

            System.out.println("Student added successfully!");

            // ---------- AUTO GENERATE PREVIOUS SEMESTER RESULTS ----------

            Random rand = new Random();

            for (int s = 1; s < sem; s++) {

                String[] subjects = AdminDashboard.getSubjectsBySemester(String.valueOf(s));

                for (String sub : subjects) {

                    int marks = 50 + rand.nextInt(50); // random 50–100

                    String grade = AdminDashboard.calculateGrade(marks);

                    PreparedStatement ps = con.prepareStatement(
                            "INSERT IGNORE INTO results VALUES(?,?,?,?,?)");

                    ps.setString(1, roll);
                    ps.setInt(2, s);
                    ps.setString(3, sub);
                    ps.setInt(4, marks);
                    ps.setString(5, grade);

                    ps.executeUpdate();
                }
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
    static void deleteStudent() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll to Delete: ");
            String roll = sc.nextLine();

            // 1️⃣ Delete results first
            PreparedStatement pst1 = con.prepareStatement(
                    "DELETE FROM results WHERE roll=?");

            pst1.setString(1, roll);
            pst1.executeUpdate();

            // 2️⃣ Then delete student
            PreparedStatement pst2 = con.prepareStatement(
                    "DELETE FROM students WHERE roll=?");

            pst2.setString(1, roll);

            int rows = pst2.executeUpdate();

            if (rows > 0)
                System.out.println("Student deleted successfully!");
            else
                System.out.println("Student not found.");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateStudent(){

        try(Connection con = DBConnection.getConnection()){

            System.out.print("Roll: ");
            String roll = sc.nextLine();

            System.out.print("New Name: ");
            String name = sc.nextLine();

            System.out.print("New Dept: ");
            String dept = sc.nextLine();

            System.out.print("New Sem: ");
            int sem = sc.nextInt();
            sc.nextLine();

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE students SET name=?,department=?,semester=? WHERE roll=?");

            pst.setString(1,name);
            pst.setString(2,dept);
            pst.setInt(3,sem);
            pst.setString(4,roll);

            pst.executeUpdate();

            System.out.println("Updated!");

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }

    public static void viewAll() {

        try (Connection con = DBConnection.getConnection()) {

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(
                    "SELECT * FROM students ORDER BY CAST(roll AS UNSIGNED)");

            System.out.println("\n==================== STUDENT LIST ====================");

            System.out.printf("%-10s %-20s %-20s %-10s\n",
                    "ROLL", "NAME", "DEPARTMENT", "SEM");

            System.out.println("------------------------------------------------------");

            while (rs.next()) {

                System.out.printf("%-10s %-20s %-20s %-10d\n",
                        rs.getString("roll"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("semester"));
            }

            System.out.println("======================================================");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}