package lithium.openstud.driver.exceptions;

public class OpenstudUserNotEnabledException extends Exception {
    public OpenstudUserNotEnabledException(String message) {
        super(message);
    }

    public OpenstudUserNotEnabledException(Exception e) {
        super(e);
    }
}
