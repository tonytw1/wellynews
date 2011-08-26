package nz.co.searchwellington.model;

public class PublisherContentCount implements Comparable<PublisherContentCount> {

	private String publisherName;
	private int count;
	
	public PublisherContentCount(String publisherName, int count) {		
		this.publisherName = publisherName;
		this.count = count;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
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
