package nz.co.searchwellington.forms;

import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

public class EditNewsitem {

    @NotBlank
    private String title, url;

    private String description;

    @NotBlank
    private String date;

    private String geocode, selectedGeocode;

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

    public String getSelectedGeocode() {
        return selectedGeocode;
    }

    public void setSelectedGeocode(String selectedGeocode) {
        this.selectedGeocode = selectedGeocode;
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
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", geocode='" + geocode + '\'' +
                ", selectedGeocode='" + selectedGeocode + '\'' +
                ", tags=" + tags +
                '}';
    }
}