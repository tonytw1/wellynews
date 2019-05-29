package nz.co.searchwellington.model;

public class PublisherContentCount implements Comparable<PublisherContentCount> {

	private String publisherName;
	private long count;
	
	public PublisherContentCount(String publisherName, long count) {
		this.publisherName = publisherName;
		this.count = count;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
	public int compareTo(PublisherContentCount event) {       
        if (event instanceof PublisherContentCount) {
            if (this.publisherName == null) {                
                return -1;
            }
            return this.publisherName.compareTo(((PublisherContentCount) event).getPublisherName());
        }        
        return 0;
    }
	
}
