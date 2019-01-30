package lithium.openstud.driver.core.models;

import java.util.Objects;

public class PaymentDescription {
    private String description;
    private Double amount;
    private Double amountPaid;
    private String academicYear;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    @Override
    public String toString() {
        return "PaymentDescription{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", amountPaid=" + amountPaid +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDescription that = (PaymentDescription) o;
        return Objects.equals(description, that.description) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(amountPaid, that.amountPaid) &&
                Objects.equals(academicYear, that.academicYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount, amountPaid, academicYear);
    }
}
