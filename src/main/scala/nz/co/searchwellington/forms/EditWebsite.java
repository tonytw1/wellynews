package nz.co.searchwellington.forms;

import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class EditWebsite {

    @NotBlank
    private String title, url;

    private String description;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "EditWebsite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", geocode='" + geocode + '\'' +
                ", osm='" + osm + '\'' +
                ", tags=" + tags +
                '}';
    }

}