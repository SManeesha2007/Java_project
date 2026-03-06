import java.sql.*;
import java.util.Scanner;

public class StudentDashboard {

    static Scanner sc = new Scanner(System.in);

    // ---------------- STUDENT LOGIN ----------------
    public static void studentLogin() {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll Number: ");
            String roll = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM students WHERE roll=? AND password=?");
            pst.setString(1, roll);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("Login Successful!");
                studentMenu(roll);
            } else {
                System.out.println("Invalid Roll or Password!");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- STUDENT MENU ----------------
    public static void studentMenu(String roll) {

        while (true) {
            System.out.println("\n----- STUDENT DASHBOARD -----");
            System.out.println("1. View My Details");
            System.out.println("2. View My Results");
            System.out.println("3. Change Password");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewDetails(roll);
                case 2 -> viewMyResults(roll);
                case 3 -> changePassword(roll);
                case 4 -> { return; }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    // ---------------- VIEW DETAILS ----------------
    static void viewDetails(String roll) {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM students WHERE roll=?");
            pst.setString(1, roll);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\n--- MY DETAILS ---");
                System.out.println("Roll      : " + rs.getString("roll"));
                System.out.println("Name      : " + rs.getString("name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Semester  : " + rs.getString("semester"));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- VIEW RESULTS ----------------
    public static void viewMyResults(String roll) {

        try {
            Connection con = DBConnection.getConnection();

            // Get current semester of student
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT semester FROM students WHERE roll = ?");
            ps1.setString(1, roll);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                System.out.println("Student not found!");
                return;
            }

            int currentSem = rs1.getInt("semester");

            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT semester, subject, marks, grade FROM results " +
                    "WHERE roll = ? ORDER BY semester");
            ps2.setString(1, roll);
            ResultSet rs = ps2.executeQuery();

            int previousSem = -1;
            double totalCGPA = 0;
            int semCount = 0;

            double semTotalPoints = 0;
            int semSubjects = 0;

            while (rs.next()) {

                int sem = rs.getInt("semester");

                if (sem != previousSem) {

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
                    System.out.println("Subject                     Marks   Grade");
                    System.out.println("-----------------------------------------");

                    previousSem = sem;
                }

                String subject = rs.getString("subject");
                int marks = rs.getInt("marks");
                String grade = rs.getString("grade");

                System.out.printf("%-25s %-7d %-5s\n",
                        subject, marks, grade);

                double points = grade != null ? getGradePoint(grade) : 0;
                semTotalPoints += points;
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

            // Final CGPA
            if (semCount > 0) {
                double cgpa = totalCGPA / semCount;
                System.out.printf("\nFINAL CGPA: %.2f\n", cgpa);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- CHANGE PASSWORD ----------------
    static void changePassword(String roll) {

        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter New Password: ");
            String newPass = sc.nextLine();

            PreparedStatement pst = con.prepareStatement(
                "UPDATE students SET password=? WHERE roll=?");
            pst.setString(1, newPass);
            pst.setString(2, roll);

            pst.executeUpdate();

            System.out.println("Password Updated Successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
}
    private static double getGradePoint(String grade) {
        return switch (grade) {
            case "A+" -> 10;
            case "A"  -> 9;
            case "B"  -> 8;
            case "C"  -> 7;
            case "F"  -> 0;
            default   -> 0;
        };
    }
}