package nz.co.searchwellington.forms;

import jakarta.validation.constraints.NotBlank;

public class NewTag {

    @NotBlank
    private String displayName;

    private String description;

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

    @Override
    public String toString() {
        return "NewTag{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}
