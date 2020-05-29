package speakeridentification.model.exceptions;

public class SoxException extends RuntimeException {

    public SoxException(String message) {
        super(message);
    }

    public SoxException(String message, Throwable cause) {
        super(message, cause);
    }

}
