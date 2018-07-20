package lithium.openstud.driver.core;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Tax {
    private int code;
    private int codeCourse;
    private String descriptionCourse;
    private int amount;
    private LocalDate paymentDate;
    private List<PaymentDescription> paymentDescriptionList;

    public Tax() {
        paymentDescriptionList = new LinkedList<PaymentDescription>();
    }

    public int getCode() {
        return code;
    }

    protected void setCode(int code) {
        this.code = code;
    }

    public int getCodeCourse() {
        return codeCourse;
    }

    protected void setCodeCourse(int codeCourse) {
        this.codeCourse = codeCourse;
    }

    public String getDescriptionCourse() {
        return descriptionCourse;
    }

    protected void setDescriptionCourse(String descriptionCourse) {
        this.descriptionCourse = descriptionCourse;
    }

    public int getAmount() {
        return amount;
    }

    protected void setAmount(int amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    protected void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public List<PaymentDescription> getPaymentDescriptionList() {
        return paymentDescriptionList;
    }

    protected void setPaymentDescriptionList(List<PaymentDescription> paymentDescriptionList) {
        this.paymentDescriptionList = paymentDescriptionList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tax tax = (Tax) o;
        return code == tax.code &&
                codeCourse == tax.codeCourse &&
                amount == tax.amount &&
                Objects.equals(descriptionCourse, tax.descriptionCourse) &&
                Objects.equals(paymentDate, tax.paymentDate) &&
                Objects.equals(paymentDescriptionList, tax.paymentDescriptionList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, codeCourse, descriptionCourse, amount, paymentDate, paymentDescriptionList);
    }

    @Override
    public String toString() {
        return "Tax{" +
                "code=" + code +
                ", codeCourse=" + codeCourse +
                ", descriptionCourse='" + descriptionCourse + '\'' +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentDescriptionList=" + paymentDescriptionList +
                '}';
    }
}

