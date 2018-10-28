package lithium.openstud.driver.core;


import org.threeten.bp.LocalDateTime;

public class Lesson {
    private String name;
    private String where;
    private LocalDateTime start;
    private LocalDateTime end;
    private String teacher;

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getWhere() {
        return where;
    }

    protected void setWhere(String where) {
        this.where = where;
    }

    public LocalDateTime getStart() {
        return start;
    }

    protected void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    protected void setEnd(LocalDateTime end) {
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
