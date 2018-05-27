package lithium.openstud.driver.core;

import java.util.Date;

public class ExamPassed extends Exam {
    private Date date;
    private int year;
    private String nominalResult;
    private int result;

    public Date getDate() {
        return date;
    }

    protected void setDate(Date date) {
        this.date = date;
    }

    public int getYear() {
        return year;
    }

    protected void setYear(int year) {
        this.year = year;
    }

    public String getNominalResult() {
        return nominalResult;
    }

    protected void setNominalResult(String nominalResult) {
        this.nominalResult = nominalResult;
    }

    public int getResult() {
        return result;
    }

    protected void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ExamPassed{" +
                "description='" + getDescription() + '\'' +
                "date=" + date +
                ", year=" + year +
                ", nominalResult='" + nominalResult + '\'' +
                ", result=" + result +
                ", subjectCode='" + getExamCode() + '\'' +
                ", ssd='" + getSsd() + '\'' +
                ", cfu=" + getCfu() +
                '}';
    }
}
