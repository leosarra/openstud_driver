package lithium.openstud.driver.exceptions;

public abstract class OpenstudBaseLoginException extends Exception {
    public enum Type {
        INVALID_PASSWORD, EXPIRED_PASSWORD;
    }
    Type type;

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

    public boolean isPasswordExpired(){
        return type == Type.EXPIRED_PASSWORD;
    }

    public boolean isPasswordInvalid(){
        return type == Type.INVALID_PASSWORD;
    }

    Exception setPasswordExpiredType(){
        type = Type.EXPIRED_PASSWORD;
        return this;
    }

    Exception setPasswordInvalidType(){
        type = Type.INVALID_PASSWORD;
        return this;
    }


}
