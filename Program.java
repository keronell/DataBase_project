import exceptions.ExamException;
import exceptions.MultiQuestionException;
import interfaces.Examable;

import java.io.*;
import java.sql.*;

import java.util.Scanner;

/**
 * @author Raz Natanzon
 * Roi Dor
 * Vladislav Pavlyuk
 * @version 1.2
 */
public class Program {
    static UserManager userManager = new UserManager(); // Create a static instance of UserManager
    public static Scanner sc = new Scanner(System.in);
    public static DataBase dataBase = new DataBase();
    public static int userID;



    private static boolean isInArray(int[] track, int number) {
        if (number < 0) // negative numbers can't be as indexes
            return false;
        if (number > dataBase.getNumOfQuestions() && number > dataBase.getNumOfAnswers()) // the number can't be bigger than the amount of questions/answers in the database
            return false;
        for (int i = 0; i < track.length; i++) { // check if the number is already in the array
            if (track[i] == number)
                return true;
        }
        return false;
    }

    private static int questionSelection(int[] trackQuestions) {
        int questionNumber = -1;
        while (questionNumber <= 0 || questionNumber > dataBase.getNumOfQuestions()) {
            printAllQuestionsWithAnswers();
            System.out.print("Enter the index of the question you want to add to the exam: ");
            questionNumber = getIntegerFromUser();
            if (questionNumber <= 0 || questionNumber > dataBase.getNumOfQuestions()) {
                System.out.println("No such index exists in the database, please try again:");
                enterCToContinue();
            } else if (isInArray(trackQuestions, questionNumber)) {
                System.out.println("You have already selected this question. No duplicates allowed, please select another question:");
                questionNumber = -1;
                enterCToContinue();
            }
        }
        return questionNumber;
    }

    private static void answerSelection(DataBase examDataBase) {
        int[] trackAnswers = new int[10];
        boolean isCorrect;
        int counter = 0;
        int answerNumber;
        while (counter < 10) {
            printAllAnswersInDatabase();
            System.out.println("Select the answers you want to add to the question: ");
            System.out.println("Enter 0 to stop selecting answers");
            answerNumber = getIntegerFromUser();
            if (isInArray(trackAnswers, answerNumber) && answerNumber != 0) {
                System.out.println("You have already selected this answer, no duplicates allowed, please select another answer:");
            } else if (answerNumber > dataBase.getNumOfAnswers() || answerNumber < 0) {
                System.out.println("No such index exists in the database, please try again:");
            } else if (answerNumber != 0) {
                System.out.println("Is this answer correct? (true/false)");
                isCorrect = getBooleanFromUser();
                if (examDataBase.getQuestion().addAnswer(dataBase.getAnswer(answerNumber - 1), isCorrect)) {
                    trackAnswers[counter++] = answerNumber;
                    System.out.println("Answer got added successfully!");
                }
            }
            if (answerNumber == 0) {
                if (counter < 4) {
                    System.out.println("You have to select at least 4 answers for each question");
                } else
                    break;
            }
            enterCToContinue();
        }
    }

    private static int createExamMultiQuestion(int questionNumber, DataBase examDataBase, int i) {
        int selectMode;
        MultiQuestion tmp = (MultiQuestion) dataBase.getQuestion(questionNumber - 1);
        if (tmp.getNumOfAnswers() > 0) {
            System.out.println("Enter 1 to select answers for this question");
            System.out.println("Enter 2 to add the question with the pre-loaded answers");
            try {
                selectMode = getIntegerFromUser();
                if (selectMode == 2)
                    if (tmp.getNumOfAnswers() < 4)
                        throw new MultiQuestionException();
            } catch (MultiQuestionException e) {
                System.out.println("Error: " + e.getMessage());
                selectMode = 1;
            }
        } else // if the question doesn't have any default answers, the user can only select answers from the database
            selectMode = 1;

        switch (selectMode) {
            case 1 -> {
                System.out.printf("Would you like to change the difficulty of the question? (true/false)\nCurrent difficulty:%s\n", tmp.getDifficulty());
                boolean changeDifficulty = getBooleanFromUser();
                if (changeDifficulty) {
                    System.out.println("Enter the new difficulty of the question:");
                    System.out.println("1 - Easy\n2 - Medium\n3 - Hard");
                    int difficulty = getIntegerFromUser();
                    while (difficulty < 1 || difficulty > 3) {
                        System.out.println("Invalid choice, please try again");
                        difficulty = getIntegerFromUser();
                    }
                    tmp.setDifficulty(BaseQuestion.questionDifficulty.values()[difficulty - 1]); // set the new difficulty
                }
                examDataBase.addQuestion(new MultiQuestion(tmp.getQuestion(), tmp.getDifficulty()));
                answerSelection(examDataBase);
                System.out.println("Question added successfully");
                enterCToContinue();
            }
            case 2 -> {
                examDataBase.addQuestion(new MultiQuestion(tmp));
                System.out.println("Question added successfully");
                enterCToContinue();
            }
            default -> {
                System.out.println("Invalid choice, please try again");
                i--;
                enterCToContinue();
            }
        }
        return i;
    }

    private static void createExamOpenQuestion(int questionNumber, DataBase examDataBase) {
        boolean changeAnswer;
        OpenQuestion tmp = (OpenQuestion) dataBase.getQuestion(questionNumber - 1);
        BaseQuestion.questionDifficulty difficulty = dataBase.getQuestion(questionNumber - 1).getDifficulty();
        if (tmp.getOpenAnswer() == null) {
            System.out.println("This question doesn't have a default answer, You will be prompted to add one");
            changeAnswer = true;
        } else {
            System.out.println("Would you like to change the default answer for this question for a new answer ? (true/false)");
            changeAnswer = getBooleanFromUser();
        }
        if (changeAnswer) {
            Answer newAnswer = addAnswer();
            System.out.println("Would you like to change the difficulty of the question? (true/false)");
            System.out.println("Current difficulty:" + difficulty);
            boolean changeDifficulty = getBooleanFromUser();
            if (changeDifficulty) {
                System.out.println("Enter the new difficulty of the question:");
                System.out.println("1 - Easy\n2 - Medium\n3 - Hard");
                difficulty = BaseQuestion.questionDifficulty.values()[getIntegerFromUser() - 1];
            }
            examDataBase.addQuestion(new OpenQuestion(tmp.getQuestion(), difficulty));
            examDataBase.getQuestion(examDataBase.getNumOfQuestions() - 1).addAnswer(newAnswer, true);
        } else {
            examDataBase.addQuestion(new OpenQuestion(tmp));
        }
        System.out.println("Question added successfully");
        enterCToContinue();
    }

    private static void createManualExam(int amountOfQuestions, DataBase examDataBase) {
        int[] trackQuestions = new int[amountOfQuestions];
        for (int i = 0; i < amountOfQuestions; i++) {
            System.out.println("[Question " + (i + 1) + "/" + amountOfQuestions + "]");
            int questionNumber = questionSelection(trackQuestions);
            trackQuestions[i] = questionNumber;
            System.out.println("You have selected the following " + "\"" + dataBase.getQuestion(questionNumber - 1).getQuestion() + "\"");
            if (dataBase.getQuestion(questionNumber - 1) instanceof OpenQuestion)
                createExamOpenQuestion(questionNumber, examDataBase);
            else if (createExamMultiQuestion(questionNumber, examDataBase, i) != i) {
                trackQuestions[i] = 0;
                i--;
            }
        }
    }
    public static void moveQtoExam(int q_id){
        Connection conn = Sql_functions.getConnection();
        if (conn == null) {
            System.out.println("Failed to establish a connection to the database.");
            return;
        }

        // Check if the question already exists in the exam_database
        String checkExistenceSql = "SELECT COUNT(*) FROM exam_database WHERE q_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkExistenceSql)) {
            checkStmt.setInt(1, q_id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Question ID " + q_id + " already exists in the exam_database. Operation aborted.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error checking existence: " + e.getMessage());
            return;
        }

        // Determine the type of the question
        String type = Sql_functions.getQuestionType(conn, q_id);
        String sqlInsert;

        if ("open".equalsIgnoreCase(type)) {
            // For open questions, assume only one answer needs to be moved.
            sqlInsert = "INSERT INTO exam_database (q_id, ans_id) " +
                    "SELECT q_id, ans_id FROM QandA WHERE q_id = ? LIMIT 1";
        } else {
            // For multiple choice questions, move all associated answers.
            sqlInsert = "INSERT INTO exam_database (q_id, ans_id) " +
                    "SELECT q_id, ans_id FROM QandA WHERE q_id = ?";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            pstmt.setInt(1, q_id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Successfully moved question and its answers to exam_database.");
            } else {
                System.out.println("No answers were moved to exam_database.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to move question and answers to exam_database: " + e.getMessage());
        }

        Sql_functions.closeConnection(conn);
    }




    public static void createExam() {

        System.out.println("-------------------------------------------");
        System.out.println("|                                         |");
        System.out.println("|            Exam Creator Tool            |");
        System.out.println("|                                         |");
        System.out.println("-------------------------------------------");
        DataBase DB = new DataBase();
        // List all id to user and choose question for the exam by ID
        // while user want to add run next line
        boolean flag = true;

        while(flag)
        {
            Sql_functions.printQuestionsAndAnswers();
            System.out.println("Enter the question ID you want to add to the exam: ");
            int q_id = getIntegerFromUser();
            Sql_functions.getQuestionByID(DB,q_id);
            moveQtoExam(q_id);
            System.out.println("Do you want to add another question? (true/false)");
            flag = getBooleanFromUser();
        }
        boolean isVaildInput = false;
        int amountOfQuestions = 0;
//         check all the prerequisites for creating an exam, prompts the user to enter the amount of questions he wants in the exam (excluding maximum of 10 questions)
//        while (!isVaildInput) {
//            try {
//                if (dataBase.getNumOfAnswers() < 4)
//                    throw new MultiQuestionException("There are not enough answers in the database to create a \"Multiple Choice Question\" with 4 answers");
//                if (dataBase.getNumOfQuestions() == 0)
//                    throw new ExamException("There are no questions in the database");
//
//                System.out.print("Enter the amount of questions you want to have in the exam: ");
//
//                amountOfQuestions = getIntegerFromUser();
//                if (amountOfQuestions <= 0)
//                    throw new ExamException("You can't have an exam with " + amountOfQuestions + " questions");
//                if (amountOfQuestions > 10)
//                    throw new ExamException();
//                if (amountOfQuestions > dataBase.getNumOfQuestions())
//                    throw new ExamException("There are only " + dataBase.getNumOfQuestions() + " questions in the database you can't have an exam with " + amountOfQuestions + " questions");
//                isVaildInput = true;
//            } catch (ExamException e) {
//                System.out.println("Exam Error: " + e.getMessage());
//                if (dataBase.getNumOfQuestions() == 0)
//                    return;
//            } catch (MultiQuestionException e) {
//                System.out.println("Multi-Question Error: " + e.getMessage());
//                return;
//            }
//        }

        Examable exam = null;
        DataBase dataBaseForExam = null;
        int examType = 0;
        System.out.println("Would you like to create a manual exam or an automatic exam?");
        System.out.println("1 - Manual Exam\n2 - Automatic Exam");
        while (examType != 1 && examType != 2) {
            examType = getIntegerFromUser();
            switch (examType) {
                case 1 -> {
                    System.out.println("You have selected a manual exam");
                    exam = new ManualExam();
                    dataBaseForExam = new DataBase();
                    createManualExam(amountOfQuestions, dataBaseForExam);
                }
                case 2 -> {
                    System.out.println("You have selected an automatic exam");
                    exam = new AutomaticExam(amountOfQuestions);
                    dataBaseForExam = dataBase;
                }
                default -> System.out.println("Invalid choice, please try again");
            }
        }
        System.out.println("Creating Exam...");
        System.out.println("Creating files in directory: " + exam.CreateExam(dataBaseForExam));
    }

    public static void deleteQuestion() {
        printAllQuestionsWithAnswers();
        System.out.println("Enter the index of the question you want to delete:");
        int questionNumber = getIntegerFromUser() - 1;
        if (questionNumber < 0 || questionNumber >= dataBase.getNumOfQuestions()) {
            System.out.println("No such index exists in the database. Please try again.");
            return;
        }
        if (dataBase.getQuestion(questionNumber) instanceof OpenQuestion) {
            dataBase.getQuestion(questionNumber).addAnswer(null, true);// unlink the answer from the question
        } else
            ((MultiQuestion) dataBase.getQuestion(questionNumber)).deleteAllAnswers(); // unlink all the answers from the question
        dataBase.deleteQuestion(questionNumber);
        System.out.println("Question got deleted successfully");
    }

    public static void deleteAnswerToQuestion() {
        printAllQuestionsWithAnswers();
        System.out.println("Enter the index of the question you want to delete an answer from: ");

        int questionNumber = getIntegerFromUser() - 1;
        if (questionNumber < 0 || questionNumber >= dataBase.getNumOfQuestions()) {
            System.out.println("No such index exists in the database. Please try again.");
            return;
        }
        if (dataBase.getQuestion(questionNumber) instanceof OpenQuestion) {
            System.out.println("This is a single answer question, you can't delete answers from it, only overwrite it");
            return;
        } else if (((MultiQuestion) dataBase.getQuestion(questionNumber)).getNumOfAnswers() == 0) {
            System.out.println("This question doesn't have any answers, you can't delete any");
            return;
        }
        System.out.print("You have selected the following ");
        System.out.println("\"" + dataBase.getQuestion(questionNumber).toStringWithAnswer() + "\"");

        System.out.println("Enter the index of the answer you want to delete: ");
        int answerNumber = getIntegerFromUser() - 1;
        if (answerNumber < 0 || answerNumber >= ((MultiQuestion) dataBase.getQuestion(questionNumber)).getNumOfAnswers()) {
            System.out.println("No such index exists in the question. Please try again.");
            return;
        }
        if (((MultiQuestion) dataBase.getQuestion(questionNumber)).deleteAnswer(answerNumber))
            System.out.println("Answer got deleted successfully");
        else
            System.out.println("Deletion of answer failed");
    }

    private static void addAnswerToMultiQuestion(int questionNumber) {//delete
        MultiQuestion tmp = (MultiQuestion) dataBase.getQuestion(questionNumber);
        boolean isCorrect;
        printAllAnswersInDatabase();
        System.out.println("Enter the index of the answer you want to add: ");
        int answerNumber = getIntegerFromUser() - 1;
        if (answerNumber < 0 || answerNumber >= dataBase.getNumOfAnswers()) {
            System.out.println("No such index exists in the database.\nReturning to main menu...");
            return;
        }
        if (tmp.doesAnswerExist(dataBase.getAnswer(answerNumber))) { // check if the answer already exists in the question
            System.out.println("This answer already exists in the question\nReturning to main menu...");
            return;
        }
        System.out.println("Is this answer correct? (true/false)");
        isCorrect = getBooleanFromUser();
        if (tmp.addAnswer(dataBase.getAnswer(answerNumber), isCorrect)) {
            System.out.println("Answer added successfully!");
        } else {
            System.out.println("You've reached the maximum number of answers for this question");
        }
    }



    private static void addAnswerToOpenQuestion(int questionNumber) {
        OpenQuestion tmp = (OpenQuestion) dataBase.getQuestion(questionNumber);
        if (tmp.getOpenAnswer() != null) {
            System.out.println("This question already has an answer,\nThe current operation will override the existing answer");
            System.out.println("Do you want to continue? (true/false)");
            if (!getBooleanFromUser()) {
                System.out.println("Returning to main menu...");
                return;
            }
        }
        printAllAnswersInDatabase();
        System.out.println("Enter the index of the answer you want to add: ");
        int answerNumber = getIntegerFromUser() - 1;
        if (answerNumber < 0 || answerNumber >= dataBase.getNumOfAnswers()) {
            System.out.println("No such index exists in the database.\nReturning to main menu...");
            return;
        }
        tmp.addAnswer(dataBase.getAnswer(answerNumber), true);
        System.out.println("Answer added successfully!");
    }


    public static void addAnswerToQuestionSQL(Connection conn,int questionNumber,int answerNumber) {
        String type = Sql_functions.getQuestionType(conn,questionNumber);
        if(type.equalsIgnoreCase("multi")){//adds another answer to the question
            System.out.println("is the answer correct?: true/false");
            boolean iscorrect = sc.nextBoolean();
            Sql_functions.addAnswerToMultipleQuestion(conn,questionNumber,answerNumber,iscorrect);
        }
        else{//it is an open question
            Sql_functions.addAnswerToOpenQuestion(conn, questionNumber,answerNumber);
        }
    }


    public static Answer addAnswer() {/////to delete
        System.out.println("Enter the answer: ");
        String answer = getStringFromUser();
        int isExist = dataBase.doesAnswerExist(answer);
        if (isExist != -1) {
            System.out.printf("The answer \"%s\" already exists in the database\n", answer);
            return dataBase.getAnswer(isExist);
        } else {
            dataBase.addAnswer(new Answer(answer));
            System.out.println("Answer added successfully");
        }
        return dataBase.getAnswer(dataBase.getNumOfAnswers() - 1);
    }


    public static void addAnswerSQL(Connection conn){

        System.out.println("Enter the answer: ");
        String answerText = getStringFromUser();
        String sql = "INSERT INTO answer (answertext) VALUES (?) RETURNING ans_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, answerText);  // Set the text of the answer in the query
            ResultSet rs = pstmt.executeQuery();  // Execute the query and get the generated keys

            // Check if an answer ID was returned and return it
            if (rs.next()) {
                int answerID = rs.getInt(1);
                System.out.println("Answer added with ID: " + answerID);
            }
        } catch (SQLException e) {
            System.out.println("Failed to add answer: " + e.getMessage());
        }



    }

    public static void addQuestionSQL() {
        Connection conn = Sql_functions.getConnection();
        System.out.println("Enter the question text:");
        String questionText = getStringFromUser();

        Difficulty difficulty = getValidDifficulty();
        QuestionType questionType = getValidQuestionType();

        String sql = "INSERT INTO question (q_text, difficulty, qtype, author_id) VALUES (?, ?::difficulty_level, ?::question_type, ?) RETURNING q_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, questionText);
            pstmt.setString(2, difficulty.name().toUpperCase());
            pstmt.setString(3, questionType.name().toUpperCase());
            pstmt.setInt(4, userID); // Set the user ID parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int questionID = rs.getInt(1);
                System.out.println("Question added with ID: " + questionID);
            }
        } catch (SQLException e) {
            System.out.println("Failed to add question: " + e.getMessage());
        }
        Sql_functions.closeConnection(conn);
    }





    public static void printAllQuestionsWithAnswers() {
        if (dataBase.getNumOfQuestions() == 0)
            System.out.println("No questions found!");
        else
            System.out.println("All the questions:");
        for (int i = 0; i < dataBase.getNumOfQuestions(); i++) {
            if (dataBase.getQuestion(i) != null)
                System.out.println((i + 1) + ".{ID:" + dataBase.getQuestion(i).getIdentifier() + " , Difficulty:" + dataBase.getQuestion(i).getDifficulty() + "}\n" + dataBase.getQuestion(i).toStringWithAnswer());
        }
    }


    public static void printAllAnswersInDatabase() {
        if (dataBase.getNumOfAnswers() == 0)
            System.out.println("No answers found!");
        else
            System.out.println("All the available answers: ");
        for (int i = 0; i < dataBase.getNumOfAnswers(); i++) {
            if (dataBase.getAnswer(i) != null)
                System.out.println((i + 1) + ". " + dataBase.getAnswer(i).toString());
        }
    }

    public static void main(String[] args) throws IOException {
        Connection conn= Sql_functions.getConnection();

        loadUsers(conn);
        int i = 0;
        for(User user: userManager.getUsers()){
            System.out.println(user);
        }
                System.out.println("Choose user: (Write user ID)");
        int userId = getIntegerFromUser();

        User user = userManager.getUserById(userId);
        if(user == null){
            System.out.println("User not found");
            return;
        }
        int choice;
        boolean flag = true;
        userID = user.getId();
        System.out.println(
                "-------------------------------------------\n" +
                        "|                                         |\n" +
                        "| Welcome to Questions And Answers System |\n" +
                        "|                                         |\n" +
                        "-------------------------------------------");
        Connection conn= Sql_functions.getConnection();//add checks for good connection
        do {

            enterCToContinue();
            menu();
            choice = getIntegerFromUser();
            switch (choice) {
                case -1 -> flag = false;
                case 1 -> Sql_functions.printQuestionsAndAnswers();//V
                case 2 -> {
                    System.out.println("Enter the answer: ");
                    String answerText = getStringFromUser();
                    addAnswerSQL(conn,answerText);//V
                }
                case 3 -> {
                    boolean inputCheck= false;
                    int questionNumber=-1,answerNumber = -1;
                    Sql_functions.printQuestionsAndAnswers();
                    while (!inputCheck) {//checks for valid Q_ID
                        System.out.println("Enter the question ID number you want to add the answer to: ");
                        questionNumber = getIntegerFromUser();
                        inputCheck=Sql_functions.questionIdExists(conn, questionNumber);
                    }
                    inputCheck=false;
                    Sql_functions.printAllAnswers(conn);
                    while(!inputCheck) {//checks for vaild answer id
                        System.out.println("choose an answer ID to add/replace");
                        answerNumber = getIntegerFromUser();
                        inputCheck= Sql_functions.answerIdExists(conn,answerNumber);
                    }
                    addAnswerToQuestionSQL(conn,questionNumber,answerNumber);//V
                }
                case 4 -> addQuestionSQL();//V
                case 5 -> {//V
                    boolean indexCheck= false;
                    int questionNumber=-1,answerNumber = -1;
                    Sql_functions.printQuestionsAndAnswers();
                    while (!indexCheck) {//checks for valid Q_ID
                        System.out.println("Enter the question ID number you want to delete the answer to: ");
                        questionNumber = getIntegerFromUser();
                        indexCheck=Sql_functions.questionIdExists(conn, questionNumber);
                    }
                    deleteAnswerToQuestionSQL(conn,questionNumber);
                }
                case 6 -> deleteQuestionSQL(conn);//V
                case 7 -> {
                    //create a database:



                    //to take each question from the qANDa datatbase and add them to DB
                    //to take each Answers from the qANDa datatbase and add them to DB
                    //to create a way to turn questions(multi or open) to objects from the database!!
                    createExam();
                }
                case 8 -> addUser();
                case 9 -> deleteUser();
                default -> System.out.println("Invalid choice");
            }
        } while (flag);
        Sql_functions.closeConnection(conn);
        System.out.println("Exiting the program...");
        System.out.println(
                "-------------------------------------------\n" +
                        "|               Goodbye !                 |\n" +
                        "|      Written by : Raz Natanzon          |\n" +
                        "|                   Roi Dor               |\n" +
                        "|                Vladislav Pavlyuk        |\n" +
                        "|                @2024                    |\n" +
                        "-------------------------------------------");
        sc.close();
    }

    public static void menu() {
        System.out.println("1. Show all questions with their answers");
        System.out.println("2. Add an answer to database");
        System.out.println("3. Add an answer from database to a question");
        System.out.println("4. Add a new question to database");
        System.out.println("5. Delete an Answer to a question");
        System.out.println("6. Delete a question (with all the answers)");
        System.out.println("7. Create Exam");
        System.out.println("8. Add User");
        System.out.println("9. Delete User");
        System.out.println("-1. Exit");
    }

    private static void deleteUser() {
        Connection conn = Sql_functions.getConnection();
        printAllUsers(conn);  // This function should query and print all users from the database
        System.out.println("Enter the ID of the user you want to delete:");
        int id = getIntegerFromUser();

        String sql = "DELETE FROM users WHERE userid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No user found with ID: " + id + ", or deletion failed.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete user: " + e.getMessage());
        }


        Sql_functions.closeConnection(conn);
    }

    public static void printAllUsers(Connection conn) {
        String sql = "SELECT userid, username, userrole FROM users ORDER BY userid";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Available Users:");
            System.out.println("ID\tUsername\tRole");
            while (rs.next()) {
                int userId = rs.getInt("userid");
                String username = rs.getString("username");
                String role = rs.getString("userrole");
                System.out.println(userId + "\t" + username + "\t" + role);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
    }

    public static void addUser() {
        Connection conn = Sql_functions.getConnection();
        System.out.println("Enter the username: ");
        String username = getStringFromUser();

        System.out.println("Enter the type of the user (ADMIN or TEACHER):");
        String userTypeInput = getUserTypeFromInput();

        // Cast the input string to the enum type explicitly in the SQL query
        String sql = "INSERT INTO users (username, userrole) VALUES (?, ?::user_role)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userTypeInput);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);  // Assuming 'userid' is an auto-increment field
                        System.out.println("User added successfully with ID: " + userId);
                    }
                }
            } else {
                System.out.println("No user was added, please check your data.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to add user: " + e.getMessage());
        }
        Sql_functions.closeConnection(conn);
    }

    private static String getUserTypeFromInput() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim().toUpperCase();
        while (!input.equals("ADMIN") && !input.equals("TEACHER")) {
            System.out.println("Invalid choice, please type 'ADMIN' or 'TEACHER':");
            input = scanner.nextLine().trim().toUpperCase();
        }
        return input;
    }




    private static String selectDatabase() {
        File folder = new File("Databases");
        String filename = "";
        if (!folder.exists())
            folder.mkdir();
        System.out.println("Select a database subject: ");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            filename = listOfFiles[i].getName();
            System.out.println((i + 1) + ". " + filename.substring(0, filename.lastIndexOf('.')));
        }
        System.out.println((listOfFiles.length + 1) + ". Create a new database");
        int choice = getIntegerFromUser();
        while (choice < 1 || choice > listOfFiles.length + 1) {
            System.out.println("Invalid input, please enter a number between 1 and " + (listOfFiles.length + 1));
            choice = getIntegerFromUser();
        }
        if (choice == listOfFiles.length + 1) {
            System.out.println("What is the name of the Subject?");
            return folder.getPath() + "/" + getStringFromUser() + ".dat";
        }
        try {
            System.out.println("Loading database... ");
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(listOfFiles[choice - 1]));
            dataBase = (DataBase) in.readObject();
            BaseQuestion.setCounter(in.readInt());
            in.close();
        } catch (Exception e) {
            System.out.println("No database found, creating a new one...");
            dataBase = new DataBase();
        }
        return listOfFiles[choice - 1].getPath();
    }

    private static void enterCToContinue() {
        System.out.println("Enter c to continue...");
        while (!sc.next().equalsIgnoreCase("c")) {
            System.out.println("Invalid input, please enter c to continue...");
        }
    }

    private static int getIntegerFromUser() {
        while (!sc.hasNextInt()) {
            System.out.println("Invalid input, please enter a number");
            sc.next();
        }
        return sc.nextInt();
    }

    private static boolean getBooleanFromUser() {
        while (!sc.hasNextBoolean()) {
            System.out.println("Invalid input, please enter true or false");
            sc.next();
        }
        return sc.nextBoolean();
    }

    private static String getStringFromUser() {
        sc.nextLine(); // clear the buffer
        return sc.nextLine();
    }

    private static Difficulty getValidDifficulty() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the difficulty (EASY, MEDIUM, HARD):");
        String input = scanner.nextLine().trim().toUpperCase();

        Difficulty difficulty = Difficulty.fromString(input); // Correct method call to parse the string
        while (difficulty == null) {
            System.out.println("Invalid input. Please choose 'EASY', 'MEDIUM', or 'HARD':");
            input = scanner.nextLine().trim().toUpperCase();
            difficulty = Difficulty.fromString(input); // Correct method call to parse the string
        }
        return difficulty;
    }



    private static QuestionType getValidQuestionType() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the question type (OPEN or MULTI):");
        String input = scanner.nextLine().trim().toUpperCase();

        QuestionType questionType = QuestionType.fromString(input);
        while (questionType == null) {
            System.out.println("Invalid input. Please choose 'OPEN' or 'MULTI':");
            input = scanner.nextLine().trim().toUpperCase();
            questionType = QuestionType.fromString(input);
        }
        return questionType;
    }



    public enum Difficulty {
        EASY, MEDIUM, HARD;

        public static Difficulty fromString(String input) {
            for (Difficulty level : values()) {
                if (level.name().equalsIgnoreCase(input)) {
                    return level;
                }
            }
            return null; // Return null if no match is found
        }
    }


    public enum QuestionType {
        OPEN, MULTI;

        public static QuestionType fromString(String str) {
            for (QuestionType type : QuestionType.values()) {
                if (type.name().equalsIgnoreCase(str)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant " + QuestionType.class.getCanonicalName() + "." + str);
        }
    }
    public static QuestionType getQuestionType(Connection conn, int questionId) {
        String sql = "SELECT qtype FROM Question WHERE q_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);  // Set the question ID parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String typeStr = rs.getString("qtype");  // Retrieve the question type as a string
                return QuestionType.fromString(typeStr);  // Convert the string to an enum and return
            } else {
                System.out.println("No question found with ID: " + questionId);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving question type: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error mapping string to enum: " + e.getMessage());
        }
        return null;  // Return null if no question is found, an error occurs, or mapping fails
    }
    public static void deleteQuestionSQL(Connection conn){
        Sql_functions.printQuestionsAndAnswers();
        System.out.println("Enter the index of the question you want to delete:");
        int questionNumber = getIntegerFromUser();

        if(Sql_functions.deleteQuestion(conn, questionNumber)){
            System.out.println("Question got deleted successfully");
        }
        else{
            System.out.println("Question deletion failed");
        }
    }

    public static void loadUsers(Connection conn) {
        String sql = "SELECT * FROM users ORDER BY userid";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("userid");
                String username = rs.getString("username");
                String type = rs.getString("userrole");

                User.UserType userType = User.UserType.valueOf(type.toUpperCase());
                userManager.addUser(new User(id, username, userType));
            }
        } catch (SQLException e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
    }



}