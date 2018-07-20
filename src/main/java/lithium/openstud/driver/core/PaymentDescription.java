package lithium.openstud.driver.core;

public class PaymentDescription {
    private String description;
    private Double amount;
    private Double amountPaid;
    private String academicYear;

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    protected void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    protected void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    protected void setAcademicYear(String academicYear) {
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
}
