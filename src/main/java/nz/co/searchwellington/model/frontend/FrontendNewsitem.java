package nz.co.searchwellington.model.frontend;

import nz.co.searchwellington.model.Geocode;

public interface FrontendNewsitem extends FrontendResource {
	
	public String getPublisherName();
	public Geocode getGeocode();

}
