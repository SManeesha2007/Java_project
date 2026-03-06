import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class ResultManagement {

    static Scanner sc = new Scanner(System.in);

    // ---------------- RESULT MANAGEMENT MENU ----------------
    public static void menu() {
        while (true) {
            System.out.println("\n--- RESULT MANAGEMENT ---");
            System.out.println("1. Add Result");
            System.out.println("2. View Results");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            int ch = Integer.parseInt(sc.nextLine().trim());

            switch (ch) {
                case 1 -> addResult();
                case 2 -> viewResults();
                case 3 -> { return; }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    // ---------------- ADD RESULT ----------------
    static void addResult() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll: ");
            String roll = sc.nextLine();

            // Get current semester
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT semester, name FROM students WHERE roll = ?");
            ps1.setString(1, roll);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                System.out.println("Student not found!");
                return;
            }

            int semester = rs1.getInt("semester");
            String name = rs1.getString("name");

            System.out.println("Adding result for: " + name + " (Semester " + semester + ")");

            String[] subjects = getSubjectsBySemester(semester);

            for (String sub : subjects) {
                System.out.print("Enter marks for " + sub + ": ");
                int marks = Integer.parseInt(sc.nextLine().trim());
                String grade = calculateGrade(marks);

                PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO results (roll, semester, subject, marks, grade) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE marks = ?, grade = ?");
                ps2.setString(1, roll);
                ps2.setInt(2, semester);
                ps2.setString(3, sub);
                ps2.setInt(4, marks);
                ps2.setString(5, grade);
                ps2.setInt(6, marks);
                ps2.setString(7, grade);

                ps2.executeUpdate();
                ps2.close();
            }

            System.out.println("Results added successfully for " + name);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- VIEW RESULTS ----------------
    static void viewResults() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll: ");
            String roll = sc.nextLine();

            // Get current semester and student name
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT semester, name FROM students WHERE roll = ?");
            ps1.setString(1, roll);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                System.out.println("Student not found!");
                return;
            }

            int currentSem = rs1.getInt("semester");
            String studentName = rs1.getString("name");

            System.out.println("\n--- RESULTS FOR: " + studentName + " (" + roll + ") ---");

            // Fetch all results ordered by semester
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT semester, subject, marks, grade FROM results " +
                    "WHERE roll = ? ORDER BY semester, subject");
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

                    // Print SGPA for previous semester
                    if (previousSem != -1) {
                        double sgpa = semSubjects > 0 ? semTotalPoints / semSubjects : 0;
                        System.out.printf("SGPA: %.2f\n", sgpa);
                        System.out.println("-----------------------------------");

                        totalCGPA += sgpa;
                        semCount++;

                        semTotalPoints = 0;
                        semSubjects = 0;
                    }

                    // Start new semester
                    System.out.println("\n========== SEMESTER " + sem + " ==========");
                    System.out.printf("%-30s %-7s %-5s\n", "Subject", "Marks", "Grade");
                    System.out.println("-----------------------------------------");

                    previousSem = sem;
                }

                String subject = rs.getString("subject");
                int marks = rs.getInt("marks");
                String grade = rs.getString("grade");

                System.out.printf("%-30s %-7d %-5s\n", subject, marks, grade);

                semTotalPoints += getGradePoint(grade);
                semSubjects++;
            }

            // Print last semester SGPA
            if (semSubjects > 0) {
                double sgpa = semSubjects > 0 ? semTotalPoints / semSubjects : 0;
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

    // ---------------- HELPER METHODS ----------------
    static String[] getSubjectsBySemester(int semester) {
        return switch (semester) {
            case 1 -> new String[]{"Mathematics-I", "Physics", "Chemistry", "English", "Basic Programming"};
            case 2 -> new String[]{"Mathematics-II", "Data Structures", "Digital Logic", "Environmental Science", "Python Programming"};
            case 3 -> new String[]{"DBMS", "Operating Systems", "Computer Networks", "Java Programming", "Software Engineering"};
            case 4 -> new String[]{"Artificial Intelligence", "Machine Learning", "Web Technologies", "Microprocessors", "Probability & Statistics"};
            case 5 -> new String[]{"Cloud Computing", "Cyber Security", "Compiler Design", "Distributed Systems", "Open Elective-I"};
            case 6 -> new String[]{"Big Data Analytics", "Data Mining", "Mobile Application Development", "IoT", "Open Elective-II"};
            case 7 -> new String[]{"DevOps", "Blockchain", "Deep Learning", "Project Phase-I", "Professional Ethics"};
            case 8 -> new String[]{"Project Phase-II", "Internship", "Seminar", "Comprehensive Viva", "Industry Training"};
            default -> new String[]{};
        };
    }

    static String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        else if (marks >= 75) return "A";
        else if (marks >= 60) return "B";
        else if (marks >= 50) return "C";
        else return "F";
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