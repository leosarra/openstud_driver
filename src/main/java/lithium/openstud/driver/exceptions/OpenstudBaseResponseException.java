package lithium.openstud.driver.exceptions;

public abstract class OpenstudBaseResponseException extends Exception {
    public enum Type {
        JSON_ERROR, MAINTENANCE, RATE_LIMIT, GENERIC
    }
    Type type;

    OpenstudBaseResponseException(String message) {
        super(message);
        type = Type.GENERIC;
    }

    OpenstudBaseResponseException(Exception e) {
        super(e);
        if (e instanceof OpenstudBaseResponseException) this.type = ((OpenstudBaseResponseException) e).type;
    }


    OpenstudBaseResponseException(OpenstudBaseResponseException e) {
        super(e);
        this.type = e.type;
    }

    public boolean isJSONError(){
        return type == Type.JSON_ERROR;
    }

    public boolean isMaintenance(){
        return type == Type.MAINTENANCE;
    }

    public boolean isRateLimit(){
        return type == Type.RATE_LIMIT;
    }

    Exception setMaintenanceType(){
        type = Type.MAINTENANCE;
        return this;
    }

    Exception setJSONType(){
        type = Type.JSON_ERROR;
        return this;
    }

    Exception setRateLimitType(){
        type = Type.RATE_LIMIT;
        return this;
    }


}
