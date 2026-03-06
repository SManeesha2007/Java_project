import java.sql.*;

public class DBConnection {

    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/student_result_UI";
            String user = "root";
            String pass = "24B11AI388";

            return DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            System.out.println("Database Connection Failed!");
            return null;
        }
    }
}