package lithium.openstud.driver.core.models;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Objects;

public class StudentCard {
    private String code;
    private LocalDate issueDate;
    private String studentId;
    private String imageBase64;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public byte[] getImage() {
        return Base64.getDecoder().decode(imageBase64);
    }

    public void setImage(byte[] image) {
        this.imageBase64 = Base64.getEncoder().encodeToString(image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentCard that = (StudentCard) o;
        return studentId == that.studentId &&
                Objects.equals(code, that.code) &&
                Objects.equals(issueDate, that.issueDate) &&
                Objects.equals(imageBase64, that.imageBase64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, studentId, issueDate, imageBase64);
    }

    @Override
    public String toString() {
        return "StudentCard{" +
                "code='" + code + '\'' +
                ", studentId=" + studentId +
                ", issueDate=" + issueDate +
                '}';
    }
}
