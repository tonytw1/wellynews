package nz.co.searchwellington.forms;

import jakarta.validation.constraints.NotBlank;

public class NewWatchlist {

    @NotBlank
    private String title, url;
    private String publisher;

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

    @Override
    public String toString() {
        return "NewWatchlist{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}