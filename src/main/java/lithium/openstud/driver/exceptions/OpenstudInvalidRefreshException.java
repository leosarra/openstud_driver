package lithium.openstud.driver.exceptions;

public class OpenstudInvalidRefreshException extends OpenstudBaseLoginException {
    public OpenstudInvalidRefreshException(String message) {
        super(message);
    }
    public OpenstudInvalidRefreshException(Exception e) { super(e); }

    @Override
    public OpenstudInvalidRefreshException setPasswordExpiredType() {
        super.setPasswordExpiredType();
        return this;
    }

    public OpenstudInvalidRefreshException setPasswordInvalidType() {
        super.setPasswordInvalidType();
        return this;
    }

}
