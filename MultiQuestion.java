/**
 * @author Daniel Grigoriev
 * @version 1.2
 */
public class MultiQuestion extends BaseQuestion {
    private final int MAX_ANSWERS = 10;
    private final Answer[] answers;
    private final boolean[] correctAnswers;
    private int numOfAnswers;
    private int numOfCorrectAnswers;

    public MultiQuestion(String question, questionDifficulty difficulty) {
        super(question, difficulty);
        answers = new Answer[MAX_ANSWERS];
        correctAnswers = new boolean[MAX_ANSWERS];
        numOfAnswers = 0;
        numOfCorrectAnswers = 0;
    }

    public MultiQuestion(MultiQuestion other) {
        super(other);
        answers = new Answer[MAX_ANSWERS];
        correctAnswers = new boolean[MAX_ANSWERS];
        numOfAnswers = 0;
        numOfCorrectAnswers = 0;
        for (int i = 0; i < other.numOfAnswers; i++) {
            this.addAnswer(other.answers[i], other.correctAnswers[i]);
        }
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public boolean[] getCorrectAnswers() {
        return correctAnswers;
    }

    public int getNumOfAnswers() {
        return numOfAnswers;
    }

    public int getNumOfCorrectAnswers() {
        return numOfCorrectAnswers;
    }

    public boolean addAnswer(Answer answer, boolean isCorrect) {
        if (numOfAnswers == MAX_ANSWERS)
            return false;
        answers[numOfAnswers] = answer;
        correctAnswers[numOfAnswers] = isCorrect;
        if (isCorrect)
            numOfCorrectAnswers++;
        numOfAnswers++;
        return true;
    }

    public boolean deleteAnswer(int answerNumber) {
        if (answerNumber < 0 || answerNumber >= numOfAnswers)
            return false;
        if (correctAnswers[answerNumber])
            numOfCorrectAnswers--;
        for (int i = answerNumber; i < numOfAnswers - 1; i++) {
            answers[i] = answers[i + 1];
            correctAnswers[i] = correctAnswers[i + 1];
        }
        numOfAnswers--;
        return true;
    }

    public void deleteAllAnswers() {
        for (int i = 0; i < numOfAnswers; i++) {
            answers[i] = null;
            correctAnswers[i] = false;
        }
    }

    public String toStringWithAnswer() {
        StringBuffer str = new StringBuffer(super.toString());
        for (int i = 0; i < numOfAnswers; i++) {
            str.append("\t" + (i + 1) + ". " + answers[i] + " (" + (correctAnswers[i] ? "correct" : "incorrect") + ")\n");
        }
        return str.toString();
    }

    public String toString() {
        StringBuffer str = new StringBuffer(super.toString());
        for (int i = 0; i < numOfAnswers; i++) {
            str.append("\t" + (i + 1) + ". " + answers[i] + "\n");
        }
        return str.toString();
    }

    public boolean doesAnswerExist(Answer answer) {
        for (int i = 0; i < numOfAnswers; i++) {
            if (answers[i].toString().equalsIgnoreCase(answer.toString()))
                return true;
        }
        return false;
    }
}
