package lithium.openstud.driver.core;

import java.util.Objects;

public class ExamDoable extends Exam {
    private String courseCode;
    private String moduleCode;

    public String getCourseCode() {
        return courseCode;
    }

    protected void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    protected void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }


    @Override
    public String toString() {
        return "ExamDoable{" +
                "description='" + getDescription() + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", moduleCode='" + moduleCode + '\'' +
                ", subjectCode='" + getExamCode() + '\'' +
                ", ssd='" + getSsd() + '\'' +
                ", cfu=" + getCfu() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamDoable that = (ExamDoable) o;
        return getCfu() == that.getCfu() &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getExamCode(), that.getExamCode()) &&
                Objects.equals(getSsd(), that.getSsd()) &&
                Objects.equals(courseCode, that.courseCode) &&
                Objects.equals(moduleCode, that.moduleCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getExamCode(), getSsd(), getCfu(), courseCode, moduleCode);
    }
}
