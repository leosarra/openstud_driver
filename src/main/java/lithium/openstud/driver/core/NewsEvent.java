package lithium.openstud.driver.core;

import java.time.LocalDate;
import java.util.Objects;

public class NewsEvent{
    private LocalDate date;
    private String description;
    private String where;
    private String room;
    private String url;

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getWhere()
    {
        return where;
    }

    public void setWhere(String where)
    {
        this.where = where;
    }

    public String getRoom()
    {
        return room;
    }

    public void setRoom(String room)
    {
        this.room = room;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NewsEvent newsEvent = (NewsEvent) o;
        return Objects.equals(date, newsEvent.date) &&
                Objects.equals(description, newsEvent.description) &&
                Objects.equals(where, newsEvent.where) &&
                Objects.equals(room, newsEvent.room) &&
                Objects.equals(url, newsEvent.url);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(date, description, where, room, url);
    }
}
