package nz.co.searchwellington.forms;

import jakarta.validation.constraints.NotBlank;

public class EditTag {

    public EditTag() {
    }

    public EditTag(String displayName, String description, String parent, String autotagHints, Boolean featured) {
        this.displayName = displayName;
        this.description = description;
        this.parent = parent;
        this.autotagHints = autotagHints;
        this.featured = featured;
    }

    @NotBlank
    private String displayName;

    private String description;

    private String parent, autotagHints;

    private Boolean featured;

    private String geocode, osm;

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getAutotagHints() {
        return autotagHints;
    }

    public void setAutotagHints(String autotagHints) {
        this.autotagHints = autotagHints;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
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
        return "EditTag{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", parent='" + parent + '\'' +
                ", autotagHints='" + autotagHints + '\'' +
                ", featured=" + featured +
                ", geocode='" + geocode + '\'' +
                ", osm='" + osm + '\'' +
                '}';
    }
}


