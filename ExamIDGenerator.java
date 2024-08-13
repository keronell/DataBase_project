public class ExamIDGenerator {
    private static int counter = 0;

    public static int generateID() {
        return ++counter;
    }
}
