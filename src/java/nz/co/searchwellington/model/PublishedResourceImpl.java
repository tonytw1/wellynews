package nz.co.searchwellington.model;

public abstract class PublishedResourceImpl extends ResourceImpl implements PublishedResource {

    protected Website publisher;

    public Website getPublisher() {
        return publisher;
    }

    public void setPublisher(Website publisher) {
        this.publisher = publisher;
    }


}
