package lithium.openstud.driver.core.models;

import org.apache.commons.codec.binary.Base64;
import org.threeten.bp.LocalDateTime;

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
        Base64 decoder = new Base64();
        return decoder.decodeBase64(imageBase64);
    }

    public void setImage(byte[] image) {
        Base64 encoder = new Base64();
        imageBase64 =  encoder.encodeBase64String(image);
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
