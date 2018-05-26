package lithium.openstud.driver;

import java.util.Date;

public class Isee {
    private double value;
    private String protocol;
    private Date dateOperation;
    private Date dateDeclaration;
    private boolean isEditable;

    public Isee(double value, String protocol, Date dateOperation, Date dateDeclaration, boolean isEditable){
        this.value=value;
        this.protocol=protocol;
        this.dateDeclaration=dateDeclaration;
        this.dateOperation=dateOperation;
        this.isEditable=isEditable;
    }

    public double getValue() {
        return value;
    }

    public String getProtocol() {
        return protocol;
    }

    public Date getDateOperation() {
        return dateOperation;
    }

    public Date getDateDeclaration() {
        return dateDeclaration;
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public String toString() {
        return "Isee{" +
                "value=" + value +
                ", protocol='" + protocol + '\'' +
                ", dateOperation=" + dateOperation +
                ", dateDeclaration=" + dateDeclaration +
                ", isEditable=" + isEditable +
                '}';
    }

    public boolean isValid(){
        if (protocol==null || protocol.isEmpty()) return false;
        else return true;
    }
}
