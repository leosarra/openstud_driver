package lithium.openstud.driver.core;

public class News {
    private String imageUrl;
    private String title;
    private String url;
    private String locale;
    private String description;

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getLocale() {
        return locale;
    }

    void setLocale(String locale) {
        this.locale = locale;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "News{" +
                "imageUrl='" + imageUrl + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", locale='" + locale + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
