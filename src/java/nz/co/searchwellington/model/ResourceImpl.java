package nz.co.searchwellington.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;



public abstract class ResourceImpl implements Resource {

    protected int id;
    protected String name;
    protected String url;
    protected int httpStatus;

    protected Date date;
    protected String description;
  
    protected Date lastScanned;
    protected Date lastChanged;
    protected Date liveTime;
    
    protected Set<Tag> tags;
    protected Snapshot snapshot;
    
    protected Set<DiscoveredFeed> discoveredFeeds;
    
    protected int technoratiCount;
    protected Geocode geocode;

    
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
    
    
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;        
    }
    
    public void getRemoveTag(Tag tag) {
    	tags.remove(tag);
	}
    
	public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }        
    }
    
    
    public SyndEntry getRssItem() {
        SyndEntry entry = new SyndEntryImpl();      
        entry.setTitle(stripIllegalCharacters(name));
        entry.setLink(url);

        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue(stripIllegalCharacters(this.description));
        entry.setDescription(description);
        return entry;
    }
        
    private String stripIllegalCharacters(String input) {
		return input.replaceAll("[^\\u0020-\\uFFFF]", "");
	}
    
	public Date getLastScanned() {
        return lastScanned;
    }
    public void setLastScanned(Date lastScanned) {
        this.lastScanned = lastScanned;
    }
    
 
    public Date getLastChanged() {
        return lastChanged;
    }
    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }
    
    public Snapshot getSnapshot() {
        return snapshot;
    }
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }
     
    public Set<DiscoveredFeed> getDiscoveredFeeds() {
        return discoveredFeeds;
    }
    public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
        this.discoveredFeeds = discoveredFeeds;
    }
    
    public Date getLiveTime() {
        return liveTime;
    }
    
    public void setLiveTime(Date liveTime) {
        this.liveTime = liveTime;
    }
    
    
	public int getTechnoratiCount() {
		return technoratiCount;
	}
	public void setTechnoratiCount(int technoratiCount) {
		this.technoratiCount = technoratiCount;
	}
	
	public Geocode getGeocode() {
		return geocode;
	}
	public void setGeocode(Geocode geocode) {
		this.geocode = geocode;
	}
	
}