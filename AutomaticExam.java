import interfaces.Examable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author Daniel Grigoriev
 * @version 1.2
 */

public class AutomaticExam implements Examable {
    private final int numOfQuestionsToGenerate;
    private DataBase allDatabase;
    private DataBase examDatabase;

    public AutomaticExam(int numOfQuestionsToGenerate) {
        this.numOfQuestionsToGenerate = numOfQuestionsToGenerate;
    }

    @Override
    public String CreateExam(Object database) {
        this.allDatabase = (DataBase) database;
        this.examDatabase = new DataBase();
        randomQuestionSelector();
        randomAnswerSelector();
        try {
            return this.create();
        } catch (FileNotFoundException e) {
            return "\n" + e.getMessage();
        }
    }

    public void randomQuestionSelector() {
        boolean[] tracker = new boolean[allDatabase.getNumOfQuestions()];
        for (int i = 0; i < numOfQuestionsToGenerate; i++) {
            int randomIndex = (int) (Math.random() * allDatabase.getNumOfQuestions());
            if (!tracker[randomIndex]) {
                if (allDatabase.getQuestion(randomIndex) instanceof MultiQuestion) {
                    examDatabase.addQuestion(new MultiQuestion(allDatabase.getQuestion(randomIndex).getQuestion(), allDatabase.getQuestion(randomIndex).difficulty));
                } else {
                    examDatabase.addQuestion(new OpenQuestion(allDatabase.getQuestion(randomIndex).getQuestion(), allDatabase.getQuestion(randomIndex).difficulty));
                }
                tracker[randomIndex] = true;
            } else {
                i--;
            }
        }
    }

    public void randomAnswerSelector() {
        int randomIndex;
        for (int i = 0; i < examDatabase.getNumOfQuestions(); i++) {
            if (examDatabase.getQuestion(i) instanceof MultiQuestion) {
                boolean oneCorrectAnswer = false;
                boolean randomBoolean;
                boolean[] tracker = new boolean[allDatabase.getNumOfAnswers()];
                for (int j = 0; j < 4; j++) {
                    randomIndex = (int) (Math.random() * allDatabase.getNumOfAnswers());
                    randomBoolean = new Random().nextBoolean();
                    if (!tracker[randomIndex]) {
                        if (randomBoolean && !oneCorrectAnswer) {
                            examDatabase.getQuestion(i).addAnswer(allDatabase.getAnswer(randomIndex), true);
                            oneCorrectAnswer = true;
                        } else {
                            examDatabase.getQuestion(i).addAnswer(allDatabase.getAnswer(randomIndex), false);
                        }
                        tracker[randomIndex] = true;
                    } else {
                        j--;
                    }
                }
            } else {
                randomIndex = (int) (Math.random() * allDatabase.getNumOfAnswers());
                examDatabase.getQuestion(i).addAnswer(allDatabase.getAnswer(randomIndex), true);
            }
        }
    }

    private String create() throws FileNotFoundException {
        LocalDateTime now = LocalDateTime.now();
        String str = now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"));
        // FOLDER
        File dir = new File("Automatic_Exams");
        dir.mkdir();

        File f = new File(dir.getPath() + "/Exam_Auto_" + str + ".txt");
        File s = new File(dir.getPath() + "/Solution_Auto_" + str + ".txt");

        // EXAM FILE
        PrintWriter pw = new PrintWriter(f);
        pw.println("**************************************************************************************");
        for (int i = 0; i < examDatabase.getNumOfQuestions(); i++) {
            pw.print((i + 1) + ". " + examDatabase.getQuestion(i).toString());
            if (examDatabase.getQuestion(i) instanceof MultiQuestion) {
                pw.println("\t" + (((MultiQuestion) examDatabase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above");
            }
            pw.println("**************************************************************************************");
        }
        pw.close();

        // SOLUTION FILE
        pw = new PrintWriter(s);
        pw.println("**************************************************************************************");
        for (int i = 0; i < examDatabase.getNumOfQuestions(); i++) {
            pw.print((i + 1) + ". " + examDatabase.getQuestion(i).toStringWithAnswer());
            if (examDatabase.getQuestion(i) instanceof MultiQuestion) {
                if (((MultiQuestion) examDatabase.getQuestion(i)).getNumOfCorrectAnswers() == 0) {
                    pw.println("\t" + (((MultiQuestion) examDatabase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above" + " (correct)");
                } else {
                    pw.println("\t" + (((MultiQuestion) examDatabase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above" + " (incorrect)");
                }
            }
            pw.println("**************************************************************************************");
        }
        pw.close();
        return dir.getAbsolutePath();
    }
}
