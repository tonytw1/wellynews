package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

public class EditWebsite {

    @NotBlank
    private String title, url;

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

    @Override
    public String toString() {
        return "EditWebsite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

}