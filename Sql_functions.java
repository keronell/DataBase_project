import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sql_functions {
    private static final String URL = "jdbc:postgresql://localhost:5432/exam_creator_project";
    private static final String USER = "postgres";  // Update with your username
    private static final String PASSWORD = "password";  // Update with your password

    public static Connection getConnection() {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Database connection URL, username, and password
            String url = "jdbc:postgresql://localhost:5432/ExamCreator_project";
            String user = "postgres";
            String password = "2012";


            // Connect to the database
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return null;
    }

    public static void closeConnection(Connection connection) {
        try {
            connection.close();
            System.out.println("Connection closed.");
        } catch (SQLException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }

    public static void loadQuestionsAndAnswers(Connection connection) {
        // Implement the logic to load questions and answers from the database
        // This method should be called after successfully connecting to the database
        // The loaded data should be stored in the appropriate data structures
    }

}

