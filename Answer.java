import java.io.Serializable;

public class Answer implements Serializable {
    private String answer;
    private static int counter = 0;
    private final int identifier;


    public Answer(String answer) {
        this.identifier = ++counter;
        this.answer = answer;
    }

    public String toString() {
        return answer;
    }

    public int getIdentifier() {
        return identifier;
    }
}
