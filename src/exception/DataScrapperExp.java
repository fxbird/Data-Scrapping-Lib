package exception;

public class DataScrapperExp extends Exception {
    public DataScrapperExp(String message) {
        super(message);
    }

    public DataScrapperExp(String message, Throwable cause) {
        super(message, cause);
    }
}
