package lithium.openstud.driver.core.models;

import org.threeten.bp.LocalDateTime;

import java.util.Base64;
import java.util.Objects;

public class StudentCard {
    private String code;
    private LocalDateTime issueDate;
    private String studentId;
    private String imageBase64;
    private boolean isEnabled;


    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

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


    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
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
        return isEnabled == that.isEnabled &&
                Objects.equals(code, that.code) &&
                Objects.equals(issueDate, that.issueDate) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(imageBase64, that.imageBase64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, issueDate, studentId, imageBase64, isEnabled);
    }

    @Override
    public String toString() {
        return "StudentCard{" +
                "code='" + code + '\'' +
                ", issueDate=" + issueDate +
                ", studentId='" + studentId + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}