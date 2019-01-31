package lithium.openstud.driver.exceptions;

public class OpenstudInvalidAnswerException extends Exception {
    public OpenstudInvalidAnswerException(String message) {
        super(message);
    }

    public OpenstudInvalidAnswerException(Exception e) {
        super(e);
    }
}
