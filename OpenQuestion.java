
public class OpenQuestion extends BaseQuestion {
    private Answer openAnswer;

    public OpenQuestion(String question, questionDifficulty difficulty) {
        super(question, difficulty);
    }

    public OpenQuestion(OpenQuestion other) {
        super(other);
        this.openAnswer = other.openAnswer;
    }

    public Answer getOpenAnswer() {
        return openAnswer;
    }

    public String toStringWithAnswer() {
        return super.toString() + "\tTextbook answer: " + (openAnswer == null ? "" : openAnswer.toString()) + "\n";
    }

    @Override
    public boolean addAnswer(Answer openAnswer, boolean isCorrect) {
        this.openAnswer = openAnswer;
        return true;
    }

    public String toString() {
        StringBuffer str = new StringBuffer(super.toString());
        str.append("\n\n");
        str.append("______________________________________________________________________________________\n\n");
        str.append("______________________________________________________________________________________\n\n");
        str.append("______________________________________________________________________________________\n\n");
        return str.toString();
    }
}
