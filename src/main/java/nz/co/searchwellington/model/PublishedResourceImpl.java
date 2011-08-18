package nz.co.searchwellington.model;

public abstract class PublishedResourceImpl extends ResourceImpl implements PublishedResource {
	
	private static final long serialVersionUID = 1L;
	
	protected Website publisher;

    public Website getPublisher() {
        return publisher;
    }

    public void setPublisher(Website publisher) {
        this.publisher = publisher;
    }

	public String getPublisherName() {
		if (publisher != null) {
			return publisher.getName();
		}
		return null;
	}
	
}
