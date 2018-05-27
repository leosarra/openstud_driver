package lithium.openstud.driver.exceptions;

public class OpenstudInvalidPasswordException extends Exception {
    public OpenstudInvalidPasswordException(String message) {
        super(message);
    }
    public OpenstudInvalidPasswordException(Exception e) { super(e); }
}
