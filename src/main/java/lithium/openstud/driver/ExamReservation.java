package lithium.openstud.driver;

import java.util.Date;

public class ExamReservation {
    private int reportID;
    private int sessionID;
    private int courseCode;
    private int cfu;
    private int reservationNumber;
    private String yearCourse;
    private String courseDescription;
    private String examSubject;
    private String teacher;
    private String department;
    private Date endDate;
    private Date startDate;
    private Date reservationDate;
    private Date examDate;
    private String note;
    private String ssd;
    private String module;

    protected ExamReservation(){

    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(int courseCode) {
        this.courseCode = courseCode;
    }

    public int getCfu() {
        return cfu;
    }

    public void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public String getYearCourse() {
        return yearCourse;
    }

    public void setYearCourse(String yearCourse) {
        this.yearCourse = yearCourse;
    }

    public int getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(int reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getExamSubject() {
        return examSubject;
    }

    public void setExamSubject(String examSubject) {
        this.examSubject = examSubject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSsd() {
        return ssd;
    }

    public void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public String toString() {
        return "ExamReservation{" +
                "examSubject='" + examSubject + '\'' +
                ", reportID=" + reportID +
                ", sessionID=" + sessionID +
                ", courseCode=" + courseCode +
                ", cfu=" + cfu +
                ", reservationNumber=" + reservationNumber +
                ", yearCourse='" + yearCourse + '\'' +
                ", courseDescription='" + courseDescription + '\'' +
                ", teacher='" + teacher + '\'' +
                ", department='" + department + '\'' +
                ", endDate=" + endDate +
                ", startDate=" + startDate +
                ", reservationDate=" + reservationDate +
                ", examDate=" + examDate +
                ", note='" + note + '\'' +
                ", ssd='" + ssd + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}
