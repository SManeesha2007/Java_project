import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class AdminDashboard {

    static Scanner sc = new Scanner(System.in);

    static String[][] semesterSubjects = {

        // Semester 1 
        {"Mathematics-I", "Physics", "Chemistry", "English", "Basic Programming"},

        // Semester 2 
        {"Mathematics-II", "Data Structures", "Digital Logic", "Environmental Science"},

        // Semester 3 
        {"DBMS", "Operating Systems", "Computer Networks", "Java Programming", "Software Engineering"},

        // Semester 4 
        {"Artificial Intelligence", "Machine Learning", "Web Technologies", "Microprocessors", "Probability & Statistics"},

        // Semester 5 
        {"Cloud Computing", "Cyber Security", "Distributed Systems", "Compiler Design", "Open Elective-I"},

        // Semester 6 
        {"Big Data Analytics", "Data Mining", "Mobile Application Development", "IoT", "Open Elective-II"},

        // Semester 7 
        {"DevOps", "Blockchain", "Deep Learning", "Project Phase-I", "Professional Ethics"},

        // Semester 8 
        {"Project Phase-II", "Internship", "Seminar", "Comprehensive Viva", "Industry Training"}
    };

    // ---------------- ADMIN LOGIN ----------------
    public static void adminLogin() {
        System.out.print("Enter Admin Username: ");
        String user = sc.nextLine();
        System.out.print("Enter Admin Password: ");
        String pass = sc.nextLine();

        if (user.equals("admin") && pass.equals("admin123")) {
            adminMenu();
        } else {
            System.out.println("Invalid Admin Credentials!");
        }
    }

    // ---------------- ADMIN MENU ----------------
    public static void adminMenu() {
        while (true) {
            System.out.println("\n----- ADMIN DASHBOARD -----");
            System.out.println("1. Student Management");
            System.out.println("2. Result Management");
            System.out.println("3. View All Students");
            System.out.println("4. View All Results (By Dept & Semester)");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (ch) {
                case 1 -> StudentManagement.menu();
                case 2 -> ResultManagement.menu();
                case 3 -> StudentManagement.viewAll();
                case 4 -> viewResultsByDeptAndSem();
                case 5 -> { 
                    System.out.println("Logging out...");
                    return; 
                }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    // Add inside AdminDashboard class

    public static String[] getSubjectsBySemester(String semester) {
        return switch (semester) {
            case "1" -> new String[]{"Mathematics-I","Physics","Chemistry","English","Basic Programming"};
            case "2" -> new String[]{"Mathematics-II","Data Structures","Digital Logic","Environmental Science","Python Programming"};
            case "3" -> new String[]{"DBMS","Operating Systems","Computer Networks","Java Programming","Software Engineering"};
            case "4" -> new String[]{"Artificial Intelligence","Machine Learning","Web Technologies","Microprocessors","Probability & Statistics"};
            case "5" -> new String[]{"Cloud Computing","Cyber Security","Compiler Design","Distributed Systems","Open Elective-I"};
            case "6" -> new String[]{"Big Data Analytics","Data Mining","Mobile Application Development","IoT","Open Elective-II"};
            case "7" -> new String[]{"DevOps","Blockchain","Deep Learning","Project Phase-I","Professional Ethics"};
            case "8" -> new String[]{"Project Phase-II","Internship","Seminar","Comprehensive Viva","Industry Training"};
            default -> new String[]{};
        };
    }

    public static String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 60) return "B";
        if (marks >= 50) return "C";
        return "F";
    }
    // ---------------- VIEW RESULTS BY DEPT & SEM ----------------
    static void viewResultsByDeptAndSem() {

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) {
                System.out.println("Database Connection Failed!");
                return;
            }

            // Get Department & Semester input
            System.out.print("Enter Department: ");
            String dept = sc.nextLine().trim();

            System.out.print("Enter Semester (1-8): ");
            int sem = Integer.parseInt(sc.nextLine().trim());

            if (sem < 1 || sem > 8) {
                System.out.println("Invalid Semester!");
                return;
            }

            String[] subjects = semesterSubjects[sem - 1];

            // Build Dynamic SQL with aliases
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.roll, s.name");

            for (String sub : subjects) {
                // Replace spaces or - with underscore for alias
                String alias = sub.replace("-", "_").replace(" ", "_");
                sql.append(", MAX(CASE WHEN r.subject='")
                .append(sub)
                .append("' THEN r.marks END) AS `")
                .append(alias)
                .append("`");
            }

            // Only fetch students who are currently in that semester
            sql.append("""
                FROM students s
                LEFT JOIN results r ON s.roll = r.roll AND r.semester = ?
                WHERE s.department = ? AND s.semester = ?
                GROUP BY s.roll, s.name
                ORDER BY CAST(s.roll AS UNSIGNED)
            """);

            PreparedStatement pst = con.prepareStatement(sql.toString());
            pst.setInt(1, sem);      // r.semester
            pst.setString(2, dept);  // s.department
            pst.setInt(3, sem);      // s.semester

            ResultSet rs = pst.executeQuery();

            // Print Header
            System.out.println("\n------------------------------------------ RESULTS -------------------------------------------------");
            System.out.printf("%-6s %-20s", "Roll", "Name");

            for (String sub : subjects) {
                System.out.printf(" %-10s", sub.length() > 8 ? sub.substring(0, 8) : sub);
            }

            System.out.println();
            System.out.println("----------------------------------------------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf("%-6s %-20s",
                        rs.getString("roll"),
                        rs.getString("name"));

                for (String sub : subjects) {
                    String alias = sub.replace("-", "_").replace(" ", "_");
                    String mark = rs.getString(alias);
                    System.out.printf(" %-10s", mark != null ? mark : "-");
                }

                System.out.println();
            }

            if (!found) {
                System.out.println("No records found.");
            }

            rs.close();
            pst.close();
            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}