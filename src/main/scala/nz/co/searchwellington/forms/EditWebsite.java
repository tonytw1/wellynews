package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

public class EditWebsite {

    @NotBlank
    private String title, url;

    private String geocode, selectedGeocode;

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

    @Override
    public String toString() {
        return "EditWebsite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", geocode='" + geocode + '\'' +
                ", selectedGeocode='" + selectedGeocode + '\'' +
                '}';
    }
}