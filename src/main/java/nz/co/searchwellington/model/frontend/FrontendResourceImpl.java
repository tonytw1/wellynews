package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;

public class FrontendResourceImpl implements FrontendResource {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
    private String type;
    private String name;
    private String url;
    private int httpStatus;    
    private Date date;
    private String description;    
    private Date liveTime;
    private List<Tag> tags;
    private List<Tag> handTags;
    private Geocode geocode;
	
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
	final public List<Tag> getTags() {
		return tags;
	}
	final public void setTags(List<Tag> tags) {
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FrontendResourceImpl other = (FrontendResourceImpl) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
