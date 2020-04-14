package nz.co.searchwellington.forms;

import org.hibernate.validator.constraints.NotBlank;

public class NewNewsitem {

    @NotBlank
    private String title, url, date, publisher;

    private String description;

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

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NewNewsitem{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }

}