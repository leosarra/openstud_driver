package lithium.openstud.driver.exceptions;

public class OpenstudInvalidResponseException extends Exception {
    public OpenstudInvalidResponseException(String message) {
        super(message);
    }
    public OpenstudInvalidResponseException(Exception e) { super(e); }
}
