package lithium.openstud.driver.core.models;

import java.util.List;
import java.util.Objects;

public class Classroom {
    private double latitude;
    private double longitude;
    private String where;
    private String name;
    private String fullName;
    private int internalId;
    private String roomId;
    private boolean occupied;
    private boolean willBeOccupied;
    private Lesson lessonNow;
    private Lesson nextLesson;
    private int weight;
    private List<Lesson> todayLessons;
    private boolean hasCoordinates;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        hasCoordinates = true;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        hasCoordinates = true;
        this.longitude = longitude;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean isWillBeOccupied() {
        return willBeOccupied;
    }

    public void setWillBeOccupied(boolean willBeOccupied) {
        this.willBeOccupied = willBeOccupied;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Lesson getLessonNow() {
        return lessonNow;
    }

    public void setLessonNow(Lesson lessonNow) {
        this.lessonNow = lessonNow;
    }

    public Lesson getNextLesson() {
        return nextLesson;
    }

    public void setNextLesson(Lesson nextLesson) {
        this.nextLesson = nextLesson;
    }

    public List<Lesson> getTodayLessons() {
        return todayLessons;
    }

    public void setTodayLessons(List<Lesson> todayLessons) {
        this.todayLessons = todayLessons;
    }

    public boolean hasCoordinates(){
        return hasCoordinates;
    }

    @Override
    public String toString() {
        return "Classroom{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", where='" + where + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", internalId=" + internalId +
                ", roomId='" + roomId + '\'' +
                ", occupied=" + occupied +
                ", willBeOccupied=" + willBeOccupied +
                ", lessonNow=" + lessonNow +
                ", nextLesson=" + nextLesson +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classroom classroom = (Classroom) o;
        return Double.compare(classroom.latitude, latitude) == 0 &&
                Double.compare(classroom.longitude, longitude) == 0 &&
                internalId == classroom.internalId &&
                occupied == classroom.occupied &&
                willBeOccupied == classroom.willBeOccupied &&
                weight == classroom.weight &&
                Objects.equals(where, classroom.where) &&
                Objects.equals(name, classroom.name) &&
                Objects.equals(fullName, classroom.fullName) &&
                Objects.equals(roomId, classroom.roomId) &&
                Objects.equals(lessonNow, classroom.lessonNow) &&
                Objects.equals(nextLesson, classroom.nextLesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, where, name, fullName, internalId, roomId, occupied, willBeOccupied, lessonNow, nextLesson, weight);
    }
}
