package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FrontendResourceImpl implements FrontendResource, RssFeedable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String urlWords;
    private String type;
    private String name;
    private String url;
    private int httpStatus;    
    private Date date;
    private String description;    
    private Date liveTime;
    private List<FrontendTag> tags;
    private List<Tag> handTags;
    private Geocode geocode;
    private Integer ownerId;
    private Place place;
	
	final public int getId() {
		return id;
	}
	final public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	final public void setType(String type) {
		this.type = type;
	}
	final public String getName() {
		return name;
	}
	final public void setName(String name) {
		this.name = name;
	}
	final public String getUrl() {
		return url;
	}
	final public void setUrl(String url) {
		this.url = url;
	}
	final public int getHttpStatus() {
		return httpStatus;
	}
	final public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	final public Date getDate() {
		return date;
	}
	final public void setDate(Date date) {
		this.date = date;
	}
	final public String getDescription() {
		return description;
	}
	final public void setDescription(String description) {
		this.description = description;
	}	
	final public Date getLiveTime() {
		return liveTime;
	}
	final public void setLiveTime(Date liveTime) {
		this.liveTime = liveTime;
	}
	final public List<FrontendTag> getTags() {
		return tags;
	}
	final public void setTags(List<FrontendTag> tags) {
		this.tags = tags;
	}
	final public List<Tag> getHandTags() {
		return handTags;
	}
	final public void setHandTags(List<Tag> handTags) {
		this.handTags = handTags;
	}
	final public Geocode getGeocode() {
		return geocode;
	}
	final public void setGeocode(Geocode geocode) {
		this.geocode = geocode;
	}	
	final public Integer getOwnerId() {
		return ownerId;
	}
	final public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	public String getUrlWords() {
		return urlWords;
	}
	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	public Place getPlace() {
		return place;
	}
	public void setPlace(Place place) {
		this.place = place;
	}
	
	public String getLocation() {
		if (place != null && place.getLatLong() != null) {
			return place.getLatLong().getLatitude() + "," + place.getLatLong().getLongitude();
		}
		return null;
	}
	
	@Override
	public String getHeadline() {
		return name;
	}
	
	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public LatLong getLatLong() {
		return place != null ? place.getLatLong() : null;	
	}
	
	@Override
	public String getWebUrl() {
		return url;
	}
	
	@Override
	public String getAuthor() {
		return null;
	}
	
}
