import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class StudentManagement {

    static Scanner sc = new Scanner(System.in);

    public static void menu() {
        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println("1. Add Student");
        System.out.println("2. Delete Student");
        System.out.println("3. Update Student");
        System.out.print("Enter choice: ");

        int ch = sc.nextInt();
        sc.nextLine(); // clear buffer

        switch (ch) {
            case 1 -> addStudent();
            case 2 -> deleteStudent();
            case 3 -> updateStudent();
            default -> System.out.println("Invalid Choice!");
        }
    }

    // ---------------- ADD STUDENT ----------------
    static void addStudent() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll: ");
            String roll = sc.nextLine().trim();
            if (roll.isEmpty()) {
                System.out.println("Roll cannot be empty!");
                return;
            }

            // Check if roll already exists
            PreparedStatement check = con.prepareStatement(
                "SELECT roll FROM students WHERE roll=?");
            check.setString(1, roll);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("Roll already exists!");
                return;
            }

            System.out.print("Enter Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Enter Department: ");
            String dept = sc.nextLine().trim();
            System.out.print("Enter Semester: ");
            int semInt = Integer.parseInt(sc.nextLine().trim()); // convert to int

            // Insert student
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO students (roll, name, department, semester, password) VALUES (?, ?, ?, ?, ?)");
            pst.setString(1, roll);
            pst.setString(2, name);
            pst.setString(3, dept);
            pst.setInt(4, semInt);
            pst.setString(5, "1234"); // default password
            pst.executeUpdate();

            System.out.println("Student Added Successfully!");
            System.out.println("Default Password is: 1234");

            // ---------------- ADD PAST SEMESTER RESULTS ----------------
            Random rand = new Random();
            for (int currentSem = 1; currentSem < semInt; currentSem++) {
                String[] subjects = AdminDashboard.getSubjectsBySemester(String.valueOf(currentSem));
                for (String sub : subjects) {
                    int marks = 60 + rand.nextInt(41); // Random 60-100
                    String grade = AdminDashboard.calculateGrade(marks);

                    PreparedStatement pstResult = con.prepareStatement(
                        "INSERT INTO results(roll, semester, subject, marks, grade) VALUES(?,?,?,?,?)"
                    );
                    pstResult.setString(1, roll);
                    pstResult.setInt(2, currentSem);
                    pstResult.setString(3, sub);
                    pstResult.setInt(4, marks);
                    pstResult.setString(5, grade);
                    pstResult.executeUpdate();
                    pstResult.close();
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- DELETE STUDENT ----------------
    static void deleteStudent() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll to Delete: ");
            String roll = sc.nextLine();

            // 1️⃣ Delete all results for this student first
            PreparedStatement pst1 = con.prepareStatement(
                "DELETE FROM results WHERE roll = ?"
            );
            pst1.setString(1, roll);
            pst1.executeUpdate();
            pst1.close();

            // 2️⃣ Then delete the student
            PreparedStatement pst2 = con.prepareStatement(
                "DELETE FROM students WHERE roll = ?"
            );
            pst2.setString(1, roll);
            int rows = pst2.executeUpdate();
            pst2.close();

            System.out.println(rows > 0 ? 
                "Deleted Successfully!" : "Roll not found!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- UPDATE STUDENT ----------------
    static void updateStudent() {
        try (Connection con = DBConnection.getConnection()) {

            System.out.print("Enter Roll to Update: ");
            String roll = sc.nextLine().trim();

            System.out.print("Enter New Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Enter New Department: ");
            String dept = sc.nextLine().trim();
            System.out.print("Enter New Semester: ");
            int semInt = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement pst = con.prepareStatement(
                "UPDATE students SET name=?, department=?, semester=? WHERE roll=?");
            pst.setString(1, name);
            pst.setString(2, dept);
            pst.setInt(3, semInt);
            pst.setString(4, roll);

            int rows = pst.executeUpdate();
            System.out.println(rows > 0 ? "Updated Successfully!" : "Roll not found!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- VIEW ALL STUDENTS ----------------
    public static void viewAll() {
        try (Connection con = DBConnection.getConnection()) {

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM students ORDER BY CAST(roll AS UNSIGNED)");

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.printf("%-10s %-20s %-15s %-10s\n", "Roll", "Name", "Department", "Semester");
            System.out.println("--------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10s %-20s %-15s %-10s\n",
                        rs.getString("roll"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("semester"));
            }

            System.out.println("--------------------------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}