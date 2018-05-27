package lithium.openstud.driver.exceptions;

public class OpenstudEndpointNotReadyException extends Exception {
    public OpenstudEndpointNotReadyException(String message) {
        super(message);
    }
    public OpenstudEndpointNotReadyException(Exception e) { super(e); }
}
