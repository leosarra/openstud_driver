package lithium.openstud.driver.core;

import java.util.Objects;

public class PaymentDescription {
    private String description;
    private int amount;
    private int year;
    private String academicYear;

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    protected void setAmount(int amount) {
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    protected void setYear(int year) {
        this.year = year;
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
                ", year=" + year +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDescription that = (PaymentDescription) o;
        return amount == that.amount &&
                year == that.year &&
                Objects.equals(description, that.description) &&
                Objects.equals(academicYear, that.academicYear);
    }

    @Override
    public int hashCode() {

        return Objects.hash(description, amount, year, academicYear);
    }
}
