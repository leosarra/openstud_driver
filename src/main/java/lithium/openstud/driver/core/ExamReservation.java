package lithium.openstud.driver.core;

import java.util.Date;

public class ExamReservation {
    private int reportID;
    private int sessionID;
    private int courseCode;
    private int cfu;
    private int reservationNumber = -1;
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

    public Date getExamDate() {
        return examDate;
    }

    protected void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    protected void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public int getReportID() {
        return reportID;
    }

    protected void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public int getSessionID() {
        return sessionID;
    }

    protected void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getCourseCode() {
        return courseCode;
    }

    protected void setCourseCode(int courseCode) {
        this.courseCode = courseCode;
    }

    public int getCfu() {
        return cfu;
    }

    protected void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public String getYearCourse() {
        return yearCourse;
    }

    protected void setYearCourse(String yearCourse) {
        this.yearCourse = yearCourse;
    }

    public int getReservationNumber() {
        return reservationNumber;
    }

    protected void setReservationNumber(int reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getExamSubject() {
        return examSubject;
    }

    protected void setExamSubject(String examSubject) {
        this.examSubject = examSubject;
    }

    public String getTeacher() {
        return teacher;
    }

    protected void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getDepartment() {
        return department;
    }

    protected void setDepartment(String department) {
        this.department = department;
    }

    public Date getEndDate() {
        return endDate;
    }

    protected void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    protected void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    protected void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getNote() {
        return note;
    }

    protected void setNote(String note) {
        this.note = note;
    }

    public String getSsd() {
        return ssd;
    }

    protected void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public String getModule() {
        return module;
    }

    protected void setModule(String module) {
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
