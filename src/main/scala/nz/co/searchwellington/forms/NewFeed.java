package nz.co.searchwellington.forms;

import nz.co.searchwellington.model.FeedAcceptancePolicy;
import jakarta.validation.constraints.NotBlank;

public class NewFeed {

    @NotBlank
    private String title, url;
    private String publisher;

    private FeedAcceptancePolicy acceptancePolicy;

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

    public FeedAcceptancePolicy getAcceptancePolicy() {
        return acceptancePolicy;
    }

    public void setAcceptancePolicy(FeedAcceptancePolicy acceptancePolicy) {
        this.acceptancePolicy = acceptancePolicy;
    }

    @Override
    public String toString() {
        return "NewFeed{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publisher='" + publisher + '\'' +
                ", acceptancePolicy=" + acceptancePolicy +
                '}';
    }

}