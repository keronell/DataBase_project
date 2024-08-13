import java.sql.*;

public class Sql_functions {
    private static final String URL = "jdbc:postgresql://localhost:5432/ExamCreator_project";
    private static final String USER = "postgres";  // Update with your username
    private static final String PASSWORD = "2012";  // Update with your password

    public static Connection getConnection() {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");


            // Connect to the database
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
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


    public static void printQuestionsAndAnswers(Connection conn) {
        String sql = "SELECT q.q_id, q.Q_text, q.difficulty, q.qtype as type, a.ans_id, a.answerText, qa.isCorrect " +
                "FROM Question q " +
                "JOIN QandA qa ON q.q_id = qa.q_id " +
                "JOIN Answer a ON qa.ans_id = a.ans_id " +  // corrected column name for consistency
                "ORDER BY q.q_id, a.ans_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int lastQId = -1; // Variable to track the last question ID processed
            String lastType = ""; // Variable to track the last question type
            char answerPrefix = 'a'; // Initial answer prefix for multiple choice questions

            while (rs.next()) {
                int qid = rs.getInt("q_id");
                String qtext = rs.getString("Q_text");
                String difficulty = rs.getString("difficulty");
                String type = rs.getString("type");
                int aid = rs.getInt("ans_id");
                String atext = rs.getString("answerText");
                boolean correct = rs.getBoolean("isCorrect");

                if (qid != lastQId) {
                    if (lastQId != -1) {
                        System.out.println(); // Add a newline between questions for clarity
                    }
                    // Print the question details
                    System.out.println(qid + ". \"" + qtext + "\" (" + difficulty + ") [" + type + "]");
                    lastQId = qid; // Update the last question ID
                    lastType = type; // Update the last question type
                    answerPrefix = 'a'; // Reset the answer prefix for a new question
                }

                if ("open".equals(type)) {
                    // Print open question answer directly
                    System.out.println("\tAnswer: " + atext + (correct ? " (Correct)" : ""));
                } else {
                    // Print multiple choice question answers with letter prefixes
                    System.out.println("\t" + answerPrefix++ + ") " + atext + (correct ? " (Correct)" : ""));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching questions and answers: " + e.getMessage());
        }

    }


    public static boolean questionIdExists(Connection conn, int questionId) {
        String sql = "SELECT 1 FROM question WHERE q_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // If rs.next() is true, the question ID exists.
        } catch (SQLException e) {
            System.out.println("Error checking if question ID exists: " + e.getMessage());
        }
        return false;  // Return false if an exception occurs or the ID does not exist.
    }

    public static boolean answerIdExists(Connection conn, int ansId){
        String sql = "SELECT 1 FROM answer WHERE ans_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ansId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // If rs.next() is true, the question ID exists.
        } catch (SQLException e) {
            System.out.println("Error checking if question ID exists: " + e.getMessage());
        }
        return false;  // Return false if an exception occurs or the ID does not exist.
    }


    public static void printAllAnswers(Connection conn) {
        String sql = "SELECT ans_id, answertext FROM Answer ORDER BY ans_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("All Answers in the Database:");
            while (rs.next()) {
                int answerId = rs.getInt("ans_id");
                String answerString = rs.getString("answertext");
                System.out.println("ID: " + answerId + " - Answer: " + answerString);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving all answers: " + e.getMessage());
        }
    }
    public static String getQuestionType(Connection conn, int questionId) {
        String sql = "SELECT qtype FROM Question WHERE q_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);  // Set the question ID parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("qtype");  // Retrieve and return the question type
            } else {
                System.out.println("No question found with ID: " + questionId);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving question type: " + e.getMessage());
        }
        return null;  // Return null if no question is found or an error occurs
    }

    public static void addAnswerToMultipleQuestion(Connection conn, int questionId, int answerId, boolean isCorrect) {
        String sql = "INSERT INTO QandA (q_id, ans_id, iscorrect) VALUES (?, ?, ?) " +
                "ON CONFLICT (q_id, ans_id) DO UPDATE SET iscorrect = EXCLUDED.iscorrect";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setInt(2, answerId);
            pstmt.setBoolean(3, isCorrect);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Answer linked to question successfully or updated existing link.");
            } else {
                System.out.println("No rows affected, data unchanged.");
            }
        } catch (SQLException e) {
            System.out.println("Error linking answer to question: " + e.getMessage());
        }
    }



    public static void addAnswerToOpenQuestion (Connection conn,int questionId, int newAnswerId){
            String sql = "UPDATE qANDa SET ans_id = ? WHERE q_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newAnswerId);
                pstmt.setInt(2, questionId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Answer to question updated successfully.");
                } else {
                    System.out.println("No rows affected. Make sure the question ID exists.");
                }
            } catch (SQLException e) {
                System.out.println("Error updating answer to question: " + e.getMessage());
            }
        }
}

