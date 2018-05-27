package lithium.openstud.driver;

public class ExamDoable {
    private String description;
    private String courseCode;
    private String moduleCode;
    private String subjectCode;
    private String ssd;
    private int cfu;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSsd() {
        return ssd;
    }

    public void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public int getCfu() {
        return cfu;
    }

    public void setCfu(int cfu) {
        this.cfu = cfu;
    }

    @Override
    public String toString() {
        return "ExamDoable{" +
                "description='" + description + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", moduleCode='" + moduleCode + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", ssd='" + ssd + '\'' +
                ", cfu=" + cfu +
                '}';
    }
}
