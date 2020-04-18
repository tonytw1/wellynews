package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

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
    private String displayName, description, parent;

    private String autotagHints;

    private Boolean featured;

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

    @Override
    public String toString() {
        return "EditTag{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", parent='" + parent + '\'' +
                ", autotagHints='" + autotagHints + '\'' +
                ", featured=" + featured +
                '}';
    }

}
