package com.resultsystem.dashboard;

import java.io.Console;
import java.sql.*;
import java.util.Scanner;

import com.resultsystem.database.DBConnection;
import com.resultsystem.management.StudentManagement;
import com.resultsystem.management.ResultManagement;
import com.resultsystem.management.InternalManagement;

public class AdminDashboard {

    static Scanner sc = new Scanner(System.in);

    static String[][] semesterSubjects = {

        {"Mathematics-I","Physics","Chemistry","English","Basic Programming"},
        {"Mathematics-II","Data Structures","Digital Logic","Environmental Science"},
        {"DBMS","Operating Systems","Computer Networks","Java Programming","Software Engineering"},
        {"Artificial Intelligence","Machine Learning","Web Technologies","Microprocessors","Probability & Statistics"},
        {"Cloud Computing","Cyber Security","Distributed Systems","Compiler Design","Open Elective-I"},
        {"Big Data Analytics","Data Mining","Mobile Application Development","IoT","Open Elective-II"},
        {"DevOps","Blockchain","Deep Learning","Project Phase-I","Professional Ethics"},
        {"Project Phase-II","Internship","Seminar","Comprehensive Viva","Industry Training"}
    };

    public static void adminLogin() {

        try(Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Admin Username: ");
            String user = sc.nextLine();

            Console console = System.console();
            String pass;

            if(console != null){
                char[] passwordArray = console.readPassword("Enter Password: ");
                pass = new String(passwordArray);
            }else{
                System.out.print("Enter Password: ");
                pass = sc.nextLine();   // fallback for IDE
            }

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM admin WHERE username=? AND password=?");

            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                System.out.println("Login Successful!");
                adminMenu();

            }else{

                System.out.println("Invalid Credentials!");
            }

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }

    public static void adminMenu() {

        while(true){

            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1 Student Management");
            System.out.println("2 Result Management");
            System.out.println("3 View All Students");
            System.out.println("4 View Results (Dept & Semester)");
            System.out.println("5 Internal Marks");
            System.out.println("6 View Revaluation Requests");
            System.out.println("7 Search Student");
            System.out.println("8 Change Password");
            System.out.println("9 Logout");
            System.out.println("===============================================");
            System.out.print(" Enter your choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch(ch){

                case 1 -> StudentManagement.menu();
                case 2 -> ResultManagement.menu();
                case 3 -> StudentManagement.viewAll();
                case 4 -> viewResultsByDeptAndSem();
                case 5 -> InternalManagement.menu();
                case 6 -> viewRevaluationRequests();
                case 7 -> searchStudent();
                case 8 -> changePassword();
                case 9 -> { return; }

                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    public static String[] getSubjectsBySemester(String semester){

        int sem = Integer.parseInt(semester);

        return semesterSubjects[sem-1];
    }

    public static String calculateGrade(int marks) {

        if (marks >= 90) return "A+";
        else if (marks >= 80) return "A";
        else if (marks >= 70) return "B+";
        else if (marks >= 60) return "B";
        else if (marks >= 50) return "C";
        else if (marks >= 40) return "D";
        else return "F";
    }

    static void viewResultsByDeptAndSem() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Enter Department: ");
            String dept = sc.nextLine();

            System.out.print("Enter Current Semester: ");
            int sem = sc.nextInt();
            sc.nextLine();

            String[] subjects = getSubjectsBySemester(String.valueOf(sem));

            StringBuilder sql = new StringBuilder();

            sql.append("SELECT s.roll, s.name");

            for (String sub : subjects) {

                sql.append(", MAX(CASE WHEN r.subject='")
                .append(sub)
                .append("' THEN r.marks END) AS `")
                .append(sub.replace(" ", "_"))
                .append("`");
            }

            sql.append("""
                FROM students s
                LEFT JOIN results r
                ON s.roll = r.roll AND r.semester = ?
                WHERE s.department = ? AND s.semester = ?
                GROUP BY s.roll, s.name
                ORDER BY CAST(s.roll AS UNSIGNED)
            """);

            PreparedStatement pst = con.prepareStatement(sql.toString());

            pst.setInt(1, sem);
            pst.setString(2, dept);
            pst.setInt(3, sem);

            ResultSet rs = pst.executeQuery();

            System.out.println();

            System.out.printf("%-12s %-20s", "ROLL", "NAME");

            for (String sub : subjects) {
                System.out.printf(" %-10s", sub.substring(0, Math.min(8, sub.length())));
            }

            System.out.println();
            System.out.println("------------------------------------------------------------------------------------------------");

            while (rs.next()) {

                System.out.printf("%-12s %-20s",
                        rs.getString("roll"),
                        rs.getString("name"));

                for (String sub : subjects) {

                    String col = sub.replace(" ", "_");

                    String mark = rs.getString(col);

                    System.out.printf(" %-10s", mark == null ? "-" : mark);
                }

                System.out.println();
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
    static void changePassword() {

        try(Connection con = DBConnection.getConnection()){

            System.out.print("Enter Username: ");
            String user = sc.nextLine();

            Console console = System.console();
            String oldPass;

            if(console != null){
                char[] passwordArray = console.readPassword("Enter Current Password: ");
                oldPass = new String(passwordArray);
            }else{
                System.out.print("Enter Current Password: ");
                oldPass = sc.nextLine();
            }

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM admin WHERE username=? AND password=?");

            ps.setString(1, user);
            ps.setString(2, oldPass);

            ResultSet rs = ps.executeQuery();

            if(!rs.next()){

                System.out.println("Incorrect current password!");
                return;
            }

            String newPass;

            if(console != null){
                char[] passwordArray = console.readPassword("Enter New Password: ");
                newPass = new String(passwordArray);
            }else{
                System.out.print("Enter New Password: ");
                newPass = sc.nextLine();
            }

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE admin SET password=? WHERE username=?");

            pst.setString(1, newPass);
            pst.setString(2, user);

            pst.executeUpdate();

            System.out.println("Password updated successfully!");

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }
    static void viewRevaluationRequests() {

        try(Connection con = DBConnection.getConnection()){

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT * FROM revaluation");

            System.out.println("\n----- REVALUATION REQUESTS -----");

            System.out.printf("%-15s %-25s %-15s %-15s\n",
                    "ROLL NUMBER","SUBJECT","STATUS","FEE");

            System.out.println("--------------------------------------------------------------");

            while(rs.next()){

                System.out.printf("%-15s %-25s %-15s %-15s\n",
                        rs.getString("roll"),
                        rs.getString("subject"),
                        rs.getString("status"),
                        rs.getString("fee"));
            }

        }catch(Exception e){

            System.out.println(e.getMessage());
        }
    }
    static void searchStudent() {

        try(Connection con = DBConnection.getConnection()){

            System.out.print("Enter Roll Number or Name: ");
            String keyword = sc.nextLine();

            PreparedStatement pst = con.prepareStatement(
            "SELECT * FROM students WHERE roll=? OR name LIKE ?");

            pst.setString(1, keyword);
            pst.setString(2, "%" + keyword + "%");

            ResultSet rs = pst.executeQuery();

            boolean found = false;

            System.out.println("\n----------- SEARCH RESULT -----------");

            while(rs.next()){

                found = true;

                System.out.println("Roll      : " + rs.getString("roll"));
                System.out.println("Name      : " + rs.getString("name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Semester  : " + rs.getInt("semester"));
                System.out.println("------------------------------------");
            }

            if(!found){
                System.out.println("Student not found.");
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}