package nz.co.searchwellington.model.frontend;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Tag;
import uk.co.eelpieconsulting.common.geo.model.Place;

public interface FrontendResource extends Serializable {

	public int getId();
	public String getType();
	public String getName();
	public String getUrl();
	public int getHttpStatus();
	public Date getDate();
	public String getDescription();
	public Date getLiveTime();
	
	public List<FrontendTag> getTags();
	public List<Tag> getHandTags();
	public Integer getOwnerId();
	public String getUrlWords();
	public void setUrlWords(String urlWords);
	public Place getPlace();
	public void setPlace(Place place);
	
	public String getLocation();	// TODO elastic search use only
	
}
