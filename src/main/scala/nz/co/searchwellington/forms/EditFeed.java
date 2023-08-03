package nz.co.searchwellington.forms;

import com.google.common.collect.Lists;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class EditFeed {

    @NotBlank
    private String title, url;
    private String publisher;

    private FeedAcceptancePolicy acceptancePolicy;

    private List<String> tags = Lists.newArrayList();

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "EditFeed{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publisher='" + publisher + '\'' +
                ", acceptancePolicy=" + acceptancePolicy +
                ", tags=" + tags +
                '}';
    }
}