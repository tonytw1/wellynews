package nz.co.searchwellington.model.frontend;

import nz.co.searchwellington.model.Geocode;

public class FrontendNewsitemImpl extends FrontendResourceImpl implements FrontendNewsitem {
	
	private String publisherName;
	
	public String getPublisherName() {
		return publisherName;
	}
	
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
	
}
