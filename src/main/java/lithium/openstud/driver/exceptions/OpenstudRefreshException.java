package lithium.openstud.driver.exceptions;

public class OpenstudRefreshException extends OpenstudBaseLoginException {
    public OpenstudRefreshException(String message) {
        super(message);
    }
    public OpenstudRefreshException(Exception e) { super(e); }

    @Override
    public OpenstudRefreshException setPasswordExpiredType() {
        super.setPasswordExpiredType();
        return this;
    }

    public OpenstudRefreshException setPasswordInvalidType() {
        super.setPasswordInvalidType();
        return this;
    }

}
