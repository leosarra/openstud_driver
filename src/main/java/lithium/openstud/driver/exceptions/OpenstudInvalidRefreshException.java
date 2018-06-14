package lithium.openstud.driver.exceptions;

public class OpenstudInvalidRefreshException extends Exception {
    public OpenstudInvalidRefreshException(String message) {
        super(message);
    }
    public OpenstudInvalidRefreshException(Exception e) { super(e); }
}
