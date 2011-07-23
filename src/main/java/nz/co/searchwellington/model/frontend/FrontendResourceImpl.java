package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;

public class FrontendResourceImpl implements FrontendResource {
	
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
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}	
	public Date getLiveTime() {
		return liveTime;
	}
	public void setLiveTime(Date liveTime) {
		this.liveTime = liveTime;
	}
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	public List<Tag> getHandTags() {
		return handTags;
	}
	public void setHandTags(List<Tag> handTags) {
		this.handTags = handTags;
	}
	public Geocode getGeocode() {
		return geocode;
	}
	
	public void setGeocode(Geocode geocode) {
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
