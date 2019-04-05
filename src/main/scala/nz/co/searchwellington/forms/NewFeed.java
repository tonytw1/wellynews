package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

public class NewFeed {

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
        return "NewFeed{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}