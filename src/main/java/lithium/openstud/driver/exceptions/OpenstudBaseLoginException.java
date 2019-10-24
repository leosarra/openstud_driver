package lithium.openstud.driver.exceptions;

public abstract class OpenstudBaseLoginException extends Exception {
    public enum Type {
        INVALID_PASSWORD, EXPIRED_PASSWORD, ACCOUNT_BLOCKED
    }

    private Type type;
    private int attemptNumber = -1;
    private int maxAttempts = -1;
    OpenstudBaseLoginException(String message) {
        super(message);
        type = Type.INVALID_PASSWORD;
    }

    OpenstudBaseLoginException(Exception e) {
        super(e);
        if (e instanceof OpenstudBaseLoginException) this.type = ((OpenstudBaseLoginException) e).type;
    }


    OpenstudBaseLoginException(OpenstudBaseLoginException e) {
        super(e);
        this.type = e.type;
    }

    public boolean isPasswordExpired() {
        return type == Type.EXPIRED_PASSWORD;
    }

    public boolean isPasswordInvalid() {
        return type == Type.INVALID_PASSWORD;
    }

    public boolean isAccountBlocked() {
        return type == Type.ACCOUNT_BLOCKED;
    }

    public int getAttemptNumber(){
        return attemptNumber;
    }

    public int getMaxAttempts(){
        return maxAttempts;
    }

    public void setAttemptNumber(int attemptNumber){
        this.attemptNumber = attemptNumber;
    }

    public void setMaxAttempts(int maxAttempts){
        this.maxAttempts = maxAttempts;
    }

    Exception setPasswordExpiredType() {
        type = Type.EXPIRED_PASSWORD;
        return this;
    }

    Exception setPasswordInvalidType() {
        type = Type.INVALID_PASSWORD;
        return this;
    }

    Exception setAccountBlockedType() {
        type = Type.ACCOUNT_BLOCKED;
        return this;
    }

}
