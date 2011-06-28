package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Tag;

public class FrontendResourceImpl implements FrontendResource {
	
    private int id;
    private String name;
    private String url;
    private int httpStatus;    
    private Date date;
    private String description;    
    private Date liveTime;
    private List<Tag> tags;
    private List<Tag> handTags;
        
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	
}
