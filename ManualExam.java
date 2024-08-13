import interfaces.Examable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManualExam implements Examable {
    private DataBase examDataBase;
    private int identifier;

    public ManualExam() {
        this.identifier = ExamIDGenerator.generateID();
    }

    @Override
    public String CreateExam(Object database) {
        examDataBase = (DataBase) database;
        try {
            return this.create();
        } catch (IOException e) {
            return "\n" + e.getMessage();
        }
    }

    public String create() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String str = now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"));
        // FOLDER
        File dir = new File("Exams");
        dir.mkdir();

        File f = new File(dir.getPath() + "/Exam_" + str + ".txt");
        File s = new File(dir.getPath() + "/Solution_" + str + ".txt");

        // EXAM FILE
        PrintWriter pw = new PrintWriter(f);
        pw.println("**************************************************************************************");
        for (int i = 0; i < examDataBase.getNumOfQuestions(); i++) {
            pw.print((i + 1) + ". " + examDataBase.getQuestion(i).toString());
            if (examDataBase.getQuestion(i) instanceof MultiQuestion) {
                pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above");
                pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 2) + ". " + "more than one answer is correct");
            }
            pw.println("**************************************************************************************");
        }
        pw.close();

        // SOLUTION FILE
        pw = new PrintWriter(s);
        pw.println("**************************************************************************************");
        for (int i = 0; i < examDataBase.getNumOfQuestions(); i++) {
            pw.print((i + 1) + ". " + examDataBase.getQuestion(i).toStringWithAnswer());
            if (examDataBase.getQuestion(i) instanceof MultiQuestion) {
                if (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfCorrectAnswers() == 0) {
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above" + " (correct)");
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 2) + ". " + "more than one answer is correct"
                            + " (incorrect)");
                } else if (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfCorrectAnswers() > 1) {
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above" + " (incorrect)");
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 2) + ". " + "more than one answer is correct" + " (correct)");
                } else {
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 1) + ". " + "None of the above" + " (incorrect)");
                    pw.println("\t" + (((MultiQuestion) examDataBase.getQuestion(i)).getNumOfAnswers() + 2) + ". " + "more than one answer is correct" + " (incorrect)");
                }
            }
            pw.println("**************************************************************************************");
        }
        pw.close();
        return dir.getAbsolutePath();
    }
}
