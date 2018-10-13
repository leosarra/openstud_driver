package lithium.openstud.driver.core;

import org.threeten.bp.LocalDate;

import java.util.Objects;

public class Student {
    private String CF;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String birthCity;
    private String birthPlace;
    private String courseYear;
    private String firstEnrollment;
    private String lastEnrollment;
    private String departmentName;
    private String courseName;
    private String nation;
    private String email;
    private String citizenship;
    private String gender;
    private String studentStatus;
    private int academicYear;
    private int academicYearCourse;
    private int studentID;
    private int codeCourse;
    private int typeStudent;
    private int cfu;
    private boolean isErasmus;

    public String getStudentStatus() {
        return studentStatus;
    }

    protected void setStudentStatus(String studentStatus) {
        this.studentStatus = studentStatus;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    protected void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getCitizenship() {
        return citizenship;
    }

    protected void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getEmail() {
        return email;
    }

    protected void setEmail(String email) {
        this.email = email;
    }

    public int getCfu() {
        return cfu;
    }

    protected void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public String getCF() {
        return CF;
    }

    protected void setCF(String CF) {
        this.CF = CF;
    }

    public String getFirstName() {
        return firstName;
    }

    protected void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    protected void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    protected void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthCity() {
        return birthCity;
    }

    protected void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getCourseYear() {
        return courseYear;
    }

    protected void setCourseYear(String courseYear) {
        this.courseYear = courseYear;
    }

    public String getFirstEnrollment() {
        return firstEnrollment;
    }

    protected void setFirstEnrollment(String firstEnrollment) {
        this.firstEnrollment = firstEnrollment;
    }

    public String getLastEnrollment() {
        return lastEnrollment;
    }

    protected void setLastEnrollment(String lastEnrollment) {
        this.lastEnrollment = lastEnrollment;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    protected void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getCourseName() {
        return courseName;
    }

    protected void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getNation() {
        return nation;
    }

    protected void setNation(String nation) {
        this.nation = nation;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    protected void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getAcademicYearCourse() {
        return academicYearCourse;
    }

    protected void setAcademicYearCourse(int academicYearCourse) {
        this.academicYearCourse = academicYearCourse;
    }

    public int getStudentID() {
        return studentID;
    }

    protected void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getCodeCourse() {
        return codeCourse;
    }

    protected void setCodeCourse(int codeCourse) {
        this.codeCourse = codeCourse;
    }

    public int getTypeStudent() {
        return typeStudent;
    }

    protected void setTypeStudent(int typeStudent) {
        this.typeStudent = typeStudent;
    }

    public String getGender() {
        return gender;
    }

    protected void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isErasmus() {
        return isErasmus;
    }

    protected void setErasmus(boolean erasmus) {
        isErasmus = erasmus;
    }

    public boolean isEnrolled(){
        if(getTypeStudent()==-1) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Student{" +
                "CF='" + CF + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", birthCity='" + birthCity + '\'' +
                ", birthPlace='" + birthPlace + '\'' +
                ", courseYear='" + courseYear + '\'' +
                ", firstEnrollment='" + firstEnrollment + '\'' +
                ", lastEnrollment='" + lastEnrollment + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", nation='" + nation + '\'' +
                ", email='" + email + '\'' +
                ", citizenship='" + citizenship + '\'' +
                ", gender='" + gender + '\'' +
                ", studentStatus='" + studentStatus + '\'' +
                ", academicYear=" + academicYear +
                ", academicYearCourse=" + academicYearCourse +
                ", studentID=" + studentID +
                ", codeCourse=" + codeCourse +
                ", typeStudent=" + typeStudent +
                ", cfu=" + cfu +
                ", isErasmus=" + isErasmus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return academicYear == student.academicYear &&
                academicYearCourse == student.academicYearCourse &&
                studentID == student.studentID &&
                codeCourse == student.codeCourse &&
                typeStudent == student.typeStudent &&
                cfu == student.cfu &&
                isErasmus == student.isErasmus &&
                Objects.equals(CF, student.CF) &&
                Objects.equals(firstName, student.firstName) &&
                Objects.equals(lastName, student.lastName) &&
                Objects.equals(birthDate, student.birthDate) &&
                Objects.equals(birthCity, student.birthCity) &&
                Objects.equals(birthPlace, student.birthPlace) &&
                Objects.equals(courseYear, student.courseYear) &&
                Objects.equals(firstEnrollment, student.firstEnrollment) &&
                Objects.equals(lastEnrollment, student.lastEnrollment) &&
                Objects.equals(departmentName, student.departmentName) &&
                Objects.equals(courseName, student.courseName) &&
                Objects.equals(nation, student.nation) &&
                Objects.equals(email, student.email) &&
                Objects.equals(citizenship, student.citizenship) &&
                Objects.equals(gender, student.gender) &&
                Objects.equals(studentStatus, student.studentStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CF, firstName, lastName, birthDate, birthCity, birthPlace, courseYear, firstEnrollment, lastEnrollment, departmentName, courseName, nation, email, citizenship, gender, studentStatus, academicYear, academicYearCourse, studentID, codeCourse, typeStudent, cfu, isErasmus);
    }
}
