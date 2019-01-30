package lithium.openstud.driver.core.models;

import org.threeten.bp.LocalDate;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Tax {
    private String code;
    private String codeCourse;
    private String descriptionCourse;
    private double amount;
    private LocalDate paymentDate;
    private LocalDate expirationDate;
    private List<PaymentDescription> paymentDescriptionList;
    private int academicYear;

    public Tax() {
        paymentDescriptionList = new LinkedList<PaymentDescription>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeCourse() {
        return codeCourse;
    }

    public void setCodeCourse(String codeCourse) {
        this.codeCourse = codeCourse;
    }

    public String getDescriptionCourse() {
        return descriptionCourse;
    }

    public void setDescriptionCourse(String descriptionCourse) {
        this.descriptionCourse = descriptionCourse;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<PaymentDescription> getPaymentDescriptionList() {
        return paymentDescriptionList;
    }

    public void setPaymentDescriptionList(List<PaymentDescription> paymentDescriptionList) {
        this.paymentDescriptionList = paymentDescriptionList;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tax tax = (Tax) o;
        return Double.compare(tax.amount, amount) == 0 &&
                academicYear == tax.academicYear &&
                Objects.equals(code, tax.code) &&
                Objects.equals(codeCourse, tax.codeCourse) &&
                Objects.equals(descriptionCourse, tax.descriptionCourse) &&
                Objects.equals(paymentDate, tax.paymentDate) &&
                Objects.equals(expirationDate, tax.expirationDate) &&
                Objects.equals(paymentDescriptionList, tax.paymentDescriptionList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, codeCourse, descriptionCourse, amount, paymentDate, expirationDate, paymentDescriptionList, academicYear);
    }

    @Override
    public String toString() {
        return "Tax{" +
                "code='" + code + '\'' +
                ", codeCourse='" + codeCourse + '\'' +
                ", descriptionCourse='" + descriptionCourse + '\'' +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", expirationDate=" + expirationDate +
                ", paymentDescriptionList=" + paymentDescriptionList +
                ", academicYear=" + academicYear +
                '}';
    }


}

