package exceptions;

/**
 * @author Daniel Grigoriev
 * @version 1.2
 */
public class ExamException extends Exception {
    public ExamException(String msg) {
        super(msg);
    }
    public ExamException() {
        super("Exam must have maximum of 10 questions!");
    }
}
