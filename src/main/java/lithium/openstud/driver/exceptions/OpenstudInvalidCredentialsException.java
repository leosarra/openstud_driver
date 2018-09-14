package lithium.openstud.driver.exceptions;

public class OpenstudInvalidCredentialsException extends Exception {
    public OpenstudInvalidCredentialsException(String message) {
        super(message);
    }
    public OpenstudInvalidCredentialsException(Exception e) { super(e); }
}
