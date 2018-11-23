package lithium.openstud.driver.exceptions;

public abstract class OpenstudBaseResponseException extends Exception {
    public enum Type {
        JSON_ERROR, MAINTENANCE, GENERIC
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

    Exception setMaintenanceType(){
        type = Type.MAINTENANCE;
        return this;
    }

    Exception setJSONType(){
        type = Type.JSON_ERROR;
        return this;
    }


}
