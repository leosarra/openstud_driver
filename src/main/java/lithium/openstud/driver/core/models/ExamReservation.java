package lithium.openstud.driver.core.models;

import org.threeten.bp.LocalDate;

import java.util.Objects;

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
    private String channel;
    private LocalDate endDate;
    private LocalDate startDate;
    private LocalDate reservationDate;
    private LocalDate examDate;
    private String note;
    private String ssd;
    private String module;

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
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
                ", channel=" + channel +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamReservation that = (ExamReservation) o;
        return reportID == that.reportID &&
                sessionID == that.sessionID &&
                courseCode == that.courseCode &&
                cfu == that.cfu &&
                reservationNumber == that.reservationNumber &&
                Objects.equals(yearCourse, that.yearCourse) &&
                Objects.equals(courseDescription, that.courseDescription) &&
                Objects.equals(examSubject, that.examSubject) &&
                Objects.equals(teacher, that.teacher) &&
                Objects.equals(department, that.department) &&
                Objects.equals(channel, that.channel) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(reservationDate, that.reservationDate) &&
                Objects.equals(examDate, that.examDate) &&
                Objects.equals(note, that.note) &&
                Objects.equals(ssd, that.ssd) &&
                Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportID, sessionID, courseCode, cfu, reservationNumber, yearCourse, courseDescription, examSubject, teacher, department, channel, endDate, startDate, reservationDate, examDate, note, ssd, module);
    }
}
