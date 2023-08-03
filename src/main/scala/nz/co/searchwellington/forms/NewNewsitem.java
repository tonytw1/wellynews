package nz.co.searchwellington.forms;

import jakarta.validation.constraints.NotBlank;

public class NewNewsitem {

    @NotBlank
    private String title, url, date, publisher;

    private String geocode, osm;
    private String description;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public String getOsm() {
        return osm;
    }

    public void setOsm(String osm) {
        this.osm = osm;
    }

    @Override
    public String toString() {
        return "NewNewsitem{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", date='" + date + '\'' +
                ", publisher='" + publisher + '\'' +
                ", geocode='" + geocode + '\'' +
                ", osm='" + osm + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}