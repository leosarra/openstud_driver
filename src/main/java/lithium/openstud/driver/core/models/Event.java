package lithium.openstud.driver.core.models;


import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;
import java.util.Objects;

public class Event {
    private EventType eventType;
    private String title;
    private String teacher;
    private LocalDateTime start; //Only lessons
    private LocalDateTime end; //Only lessons
    private ExamReservation res; //Only Doable,Reserved
    private String where; //Only lessons and theatre
    //Only Theatre
    private String description;
    private String url;
    private String imageUrl;
    private String room;

    public Event(EventType eventType) {
        this.eventType = eventType;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
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

    public ExamReservation getReservation() {
        return res;
    }

    public void setReservation(ExamReservation res) {
        this.res = res;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Timestamp getTimestamp(ZoneId zoneId) {
        if (getEventType() == EventType.LESSON || getEventType() == EventType.THEATRE) {
            if (getStart() != null)
                return new Timestamp(getStart().toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli());
            else return null;
        } else {
            if (getReservation() != null && getReservation().getExamDate() != null)
                return new Timestamp(getReservation().getExamDate().atStartOfDay(zoneId).toInstant().toEpochMilli());
            else return null;
        }
    }

    public LocalDate getEventDate() {
        if (getEventType() == EventType.LESSON || getEventType() == EventType.THEATRE) {
            if (getStart() != null) return getStart().toLocalDate();
            else return null;
        } else {
            if (getReservation() != null) return getReservation().getExamDate();
            else return null;
        }
    }


    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", title='" + title + '\'' +
                ", teacher='" + teacher + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", res=" + res +
                ", where='" + where + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", room='" + room + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return eventType == event.eventType &&
                Objects.equals(title, event.title) &&
                Objects.equals(teacher, event.teacher) &&
                Objects.equals(start, event.start) &&
                Objects.equals(end, event.end) &&
                Objects.equals(res, event.res) &&
                Objects.equals(where, event.where) &&
                Objects.equals(description, event.description) &&
                Objects.equals(url, event.url) &&
                Objects.equals(imageUrl, event.imageUrl) &&
                Objects.equals(room, event.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, title, teacher, start, end, res, where, description, url, imageUrl, room);
    }
}