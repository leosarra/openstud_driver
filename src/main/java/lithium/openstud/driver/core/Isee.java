package lithium.openstud.driver.core;


import org.threeten.bp.LocalDate;

import java.util.Objects;

public class Isee {
    private double value;
    private String protocol;
    private LocalDate dateOperation;
    private LocalDate dateDeclaration;
    private boolean isEditable;

    public double getValue() {
        return value;
    }

    public String getProtocol() {
        return protocol;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public LocalDate getDateDeclaration() {
        return dateDeclaration;
    }

    public boolean isEditable() {
        return isEditable;
    }

    protected void setValue(double value) {
        this.value = value;
    }

    protected void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    protected void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    protected void setDateDeclaration(LocalDate dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    protected void setEditable(boolean editable) {
        isEditable = editable;
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
        return protocol != null && !protocol.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Isee isee = (Isee) o;
        return Double.compare(isee.value, value) == 0 &&
                isEditable == isee.isEditable &&
                Objects.equals(protocol, isee.protocol) &&
                Objects.equals(dateOperation, isee.dateOperation) &&
                Objects.equals(dateDeclaration, isee.dateDeclaration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, protocol, dateOperation, dateDeclaration, isEditable);
    }
}
