package lithium.openstud.driver.core.models;


import org.threeten.bp.ZonedDateTime;

public class Lesson {
    private String name;
    private String where;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private String teacher;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "name='" + name + '\'' +
                ", where='" + where + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", teacher='" + teacher + '\'' +
                '}';
    }
}
