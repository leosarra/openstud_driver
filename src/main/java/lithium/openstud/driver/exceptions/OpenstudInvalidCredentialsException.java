package lithium.openstud.driver.exceptions;

public class OpenstudInvalidCredentialsException extends OpenstudBaseLoginException {
    public OpenstudInvalidCredentialsException(String message) {
        super(message);
    }

    public OpenstudInvalidCredentialsException(Exception e) {
        super(e);
        if (e instanceof OpenstudBaseLoginException) {
            OpenstudBaseLoginException obj = (OpenstudBaseLoginException) e;
            if (obj.isAccountBlocked()) this.setAccountBlockedType();
            else if (obj.isPasswordExpired()) this.setPasswordExpiredType();
            else if (obj.isPasswordInvalid()) this.setPasswordInvalidType();
        }
    }

    @Override
    public OpenstudInvalidCredentialsException setPasswordExpiredType() {
        super.setPasswordExpiredType();
        return this;
    }

    public OpenstudInvalidCredentialsException setPasswordInvalidType() {
        super.setPasswordInvalidType();
        return this;
    }

    public OpenstudInvalidCredentialsException setAccountBlockedType() {
        super.setAccountBlockedType();
        return this;
    }

}
