import java.io.Serializable;

public abstract class BaseQuestion implements Serializable {


    public enum questionDifficulty {EASY, MEDIUM, HARD;}

    private static int counter = 0;
    protected String question;
    protected int identifier;
    protected questionDifficulty difficulty;

    public BaseQuestion(String question, questionDifficulty difficulty) {
        this.question = question;
        identifier = ++counter;
        this.difficulty = difficulty;
    }

    public BaseQuestion(BaseQuestion other) {
        this.question = other.question;
        this.identifier = other.identifier;
        this.difficulty = other.difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public questionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(questionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getIdentifier() {
        return identifier;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        BaseQuestion.counter = counter;
    }

    public String toString() {
        return question + "\n";
    }

    public abstract String toStringWithAnswer();

    public abstract boolean addAnswer(Answer answer, boolean isCorrect);
}
