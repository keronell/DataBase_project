import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Daniel Grigoriev
 * @version 1.2
 */
public class DataBase implements Serializable {
    private BaseQuestion[] questions;
    private Answer[] answers;
    private int numOfQuestions;
    private int numOfAnswers;

    public DataBase() {
        questions = new BaseQuestion[100];
        answers = new Answer[100];
    }

    public int getNumOfQuestions() {
        return numOfQuestions;
    }

    public int getNumOfAnswers() {
        return numOfAnswers;
    }

    public BaseQuestion getQuestion() {
        return questions[numOfQuestions - 1];
    }

    public BaseQuestion getQuestion(int index) {
        if (index < 0 || index >= numOfQuestions) {
            return null;
        }
        return questions[index];
    }

    public Answer getAnswer(int index) {
        if (index < 0 || index >= numOfAnswers) {
            return null;
        }
        return answers[index];
    }

    public void addQuestion(BaseQuestion question) {
        if (numOfQuestions == questions.length)
            questions = Arrays.copyOf(questions, questions.length * 2);
        questions[numOfQuestions++] = question;
    }

    public void addAnswer(Answer answer) {
        if (numOfAnswers == answers.length)
            answers = Arrays.copyOf(answers, answers.length * 2);
        answers[numOfAnswers++] = answer;
    }

    public int doesAnswerExist(String answer) {
        for (int i = 0; i < numOfAnswers; i++) {
            if (answers[i].toString().equalsIgnoreCase(answer)) {
                return i;
            }
        }
        return -1;
    }

    public boolean doesQuestionExist(String question) {
        for (int i = 0; i < numOfQuestions; i++) {
            if (questions[i].getQuestion().equalsIgnoreCase(question)) {
                return true;
            }
        }
        return false;
    }

    public void deleteQuestion(int index) {
        for (int i = index; i < numOfQuestions; i++) {
            questions[i] = questions[i + 1];
        }
        numOfQuestions--;
    }
}
