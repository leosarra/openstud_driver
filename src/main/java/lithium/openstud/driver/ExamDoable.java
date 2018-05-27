package lithium.openstud.driver;

public class ExamDoable extends Exam {
    private String courseCode;
    private String moduleCode;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
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
}
