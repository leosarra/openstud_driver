package lithium.openstud.driver.core;


import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.Objects;

public class Event {
    private String title;
    private String description;
    private String teacher;
    private String where;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDate startReservations;
    private LocalDate endReservations;
    private LocalDate examDate;
    private EventType eventType;
    private ExamReservation res;
    private String url;
    private String imageUrl;
    private String room;

    public Event(String description, LocalDateTime start, LocalDateTime end, EventType eventType){
        this.description = description;
        this.start = start;
        this.end = end;
        this.eventType = eventType;
    }

    public Event(EventType eventType){
        this.eventType = eventType;
    }


    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
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

    public String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRoom() {
        return room;
    }

    void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                "description='" + description + '\'' +
                ", teacher='" + teacher + '\'' +
                ", where='" + where + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", startReservations=" + startReservations +
                ", endReservations=" + endReservations +
                ", examDate=" + examDate +
                ", eventType=" + eventType +
                ", imageUrl=" + imageUrl +
                ", url=" + url +
                ", room=" + room +
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
                Objects.equals(imageUrl, event.imageUrl) &&
                Objects.equals(url, event.url) &&
                Objects.equals(room, event.room) &&
                Objects.equals(title, event.title) &&
                eventType == event.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, teacher, where, start, end, startReservations, endReservations, examDate, eventType, imageUrl, url, room, title);
    }
}
