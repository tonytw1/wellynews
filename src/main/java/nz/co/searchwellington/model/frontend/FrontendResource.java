package nz.co.searchwellington.model.frontend;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;

public interface FrontendResource extends Serializable {

	public int getId();
	public String getType();
	public String getName();
	public String getUrl();
	public int getHttpStatus();
	public Date getDate();
	public String getDescription();
	public Date getLiveTime();
	
	public Geocode getGeocode();
	
	public List<Tag> getTags();
	public List<Tag> getHandTags();
	
}
