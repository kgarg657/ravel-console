package exception;

public class CaseAlreadyActiveException extends RuntimeException {
    public CaseAlreadyActiveException(String message) {
        super(message);
    }
}