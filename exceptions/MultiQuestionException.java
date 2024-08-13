package exceptions;
/**
 * @author Daniel Grigoriev
 * @version 1.2
 */
public class MultiQuestionException extends Exception{
    public MultiQuestionException(String msg) {
        super(msg);
    }
    public MultiQuestionException() {
        super("Multiple-Choice Questions must have at least 4 answers!");
    }
}
