package lithium.openstud.driver.core;


import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.Objects;

public class Event {
    private String description;
    private String teacher; //May come in handy in the future
    private String where; //for the future
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDate startReservations;
    private LocalDate endReservations;
    private LocalDate examDate;
    private EventType eventType;
    private ExamReservation res;


    public Event(String description, LocalDateTime start, LocalDateTime end, EventType eventType){
        this.description = description;
        this.start = start;
        this.end = end;
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime when) {
        this.start = when;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public LocalDate getStartReservations() {
        return startReservations;
    }

    public void setStartReservations(LocalDate startReservations) {
        this.startReservations = startReservations;
    }

    public LocalDate getEndReservations() {
        return endReservations;
    }

    public void setEndReservations(LocalDate endReservations) {
        this.endReservations = endReservations;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public ExamReservation getReservation() {
        return res;
    }

    public void setReservation(ExamReservation res) {
        this.res = res;
    }

    @Override
    public String toString() {
        return "Event{" +
                "description='" + description + '\'' +
                ", teacher='" + teacher + '\'' +
                ", where='" + where + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", startReservations=" + startReservations +
                ", endReservations=" + endReservations +
                ", examDate=" + examDate +
                ", eventType=" + eventType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(description, event.description) &&
                Objects.equals(teacher, event.teacher) &&
                Objects.equals(where, event.where) &&
                Objects.equals(start, event.start) &&
                Objects.equals(end, event.end) &&
                Objects.equals(startReservations, event.startReservations) &&
                Objects.equals(endReservations, event.endReservations) &&
                Objects.equals(examDate, event.examDate) &&
                eventType == event.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, teacher, where, start, end, startReservations, endReservations, examDate, eventType);
    }
}
