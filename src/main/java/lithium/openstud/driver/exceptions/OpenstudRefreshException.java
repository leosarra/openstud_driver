package lithium.openstud.driver.exceptions;

public class OpenstudRefreshException extends OpenstudBaseLoginException {
    public OpenstudRefreshException(String message) {
        super(message);
    }

    public OpenstudRefreshException(Exception e) {
        super(e);
        if (e instanceof OpenstudBaseLoginException) {
            OpenstudBaseLoginException obj = (OpenstudBaseLoginException) e;
            if (obj.isAccountBlocked()) this.setAccountBlockedType();
            else if (obj.isPasswordExpired()) this.setPasswordExpiredType();
            else if (obj.isPasswordInvalid()) this.setPasswordInvalidType();
        }
    }

    @Override
    public OpenstudRefreshException setPasswordExpiredType() {
        super.setPasswordExpiredType();
        return this;
    }

    public OpenstudRefreshException setPasswordInvalidType() {
        super.setPasswordInvalidType();
        return this;
    }

    public OpenstudRefreshException setAccountBlockedType() {
        super.setAccountBlockedType();
        return this;
    }

}
