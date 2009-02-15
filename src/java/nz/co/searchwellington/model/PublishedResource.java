package nz.co.searchwellington.model;

public interface PublishedResource extends Resource {
    
    public Website getPublisher();
   
    public void setPublisher(Website publisher);
}
