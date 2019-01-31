package lithium.openstud.driver.core.models;

import java.util.Objects;

public class NewsEvent {
    // to local date
    private String date;
    private String hour;
    private String description;
    private String where;
    private String room;
    private String url;
    private String imageUrl;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NewsEvent newsEvent = (NewsEvent) o;
        return Objects.equals(date, newsEvent.date) &&
                Objects.equals(description, newsEvent.description) &&
                Objects.equals(where, newsEvent.where) &&
                Objects.equals(room, newsEvent.room) &&
                Objects.equals(url, newsEvent.url) &&
                Objects.equals(imageUrl, newsEvent.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description, where, room, url, imageUrl);
    }

    @Override
    public String toString() {
        return "NewsEvent{" + "date=" + date + ", description='" + description + '\'' + ", where='" + where + '\'' +
                ", room='" + room + '\'' + ", url='" + url + '\'' + imageUrl + '\'' + '}';
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
