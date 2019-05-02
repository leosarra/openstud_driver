package lithium.openstud.driver.core.models;

import java.util.Objects;

public class Career {
    private int index;
    private String registrationCode;
    private String codeCourse;
    private String description;
    private String descriptionComplete;
    private String organization;
    private String type;
    private String teachingCode;

    public String getTeachingCode() {
        return teachingCode;
    }

    public void setTeachingCode(String teachingCode) {
        this.teachingCode = teachingCode;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getCodeCourse() {
        return codeCourse;
    }

    public void setCodeCourse(String codeCourse) {
        this.codeCourse = codeCourse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionComplete() {
        return descriptionComplete;
    }

    public void setDescriptionComplete(String descriptionComplete) {
        this.descriptionComplete = descriptionComplete;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Career career = (Career) o;
        return index == career.index &&
                Objects.equals(registrationCode, career.registrationCode) &&
                Objects.equals(codeCourse, career.codeCourse) &&
                Objects.equals(description, career.description) &&
                Objects.equals(descriptionComplete, career.descriptionComplete) &&
                Objects.equals(organization, career.organization) &&
                Objects.equals(type, career.type) &&
                Objects.equals(teachingCode, career.teachingCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, registrationCode, codeCourse, description, descriptionComplete, organization, type, teachingCode);
    }

    @Override
    public String toString() {
        return "Career{" +
                "index=" + index +
                ", registrationCode='" + registrationCode + '\'' +
                ", codeCourse='" + codeCourse + '\'' +
                ", description='" + description + '\'' +
                ", descriptionComplete='" + descriptionComplete + '\'' +
                ", organization='" + organization + '\'' +
                ", type='" + type + '\'' +
                ", teachingCode='" + teachingCode + '\'' +
                '}';
    }
}
