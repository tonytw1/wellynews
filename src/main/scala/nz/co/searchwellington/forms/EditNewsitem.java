package nz.co.searchwellington.forms;

import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class EditNewsitem {

    @NotBlank
    private String title, url;

    private String publisher;

    private String description;

    @NotBlank
    private String date;

    private String geocode, osm;

    private List<String> tags = Lists.newArrayList();

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "EditNewsitem{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publisher='" + publisher + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", geocode='" + geocode + '\'' +
                ", osm='" + osm + '\'' +
                ", tags=" + tags +
                '}';
    }
}