import java.sql.*;

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


    public static void printQuestionsAndAnswers() {
        Connection conn = getConnection();
        String sql = "SELECT q.q_id, q.Q_text, q.difficulty, q.qtype as type, a.ans_id, a.answerText, qa.isCorrect " +
                "FROM Question q " +
                "JOIN QandA qa ON q.q_id = qa.q_id " +
                "JOIN Answer a ON qa.\"ans_id\" = a.ans_id " +
                "ORDER BY q.q_id, a.ans_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int lastQId = -1; // Variable to track the last question ID processed
            while (rs.next()) {
                int qid = rs.getInt("q_id");
                String qtext = rs.getString("Q_text");
                String difficulty = rs.getString("difficulty");
                String type = rs.getString("type");
                int aid = rs.getInt("ans_id");
                String atext = rs.getString("answerText");
                boolean correct = rs.getBoolean("isCorrect");

                // Check if the current question ID is different from the last processed
                if (qid != lastQId) {
                    // Print the question text with difficulty and type if it's a new question
                    System.out.println("\n" + qid + ". - " + qtext + " (" + difficulty + ") [" + type + "]");
                    lastQId = qid; // Update the last question ID
                }
                // Print the answer details with a letter prefix
                System.out.println("\t" + (char)('a' + aid - 1) + ") " + atext + (correct ? " (Correct)" : ""));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching questions and answers: " + e.getMessage());
        }
        closeConnection(conn);
    }


}

