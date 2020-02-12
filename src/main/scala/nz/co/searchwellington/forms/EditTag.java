package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

public class EditTag {

    public EditTag() {
    }

    public EditTag(String displayName, String description, String parent) {
        this.displayName = displayName;
        this.description = description;
        this.parent = parent;
    }

    @NotBlank
    private String displayName, description, parent;

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

    @Override
    public String toString() {
        return "EditTag{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", parent='" + parent + '\'' +
                '}';
    }

}
