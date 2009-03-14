package nz.co.searchwellington.model.decoraters;

import java.util.Date;
import java.util.Set;

import com.sun.syndication.feed.synd.SyndEntry;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

public class ResourceWrapper implements Resource {

    protected Resource resource;
    
    public ResourceWrapper(Resource resource) {   
        this.resource = resource;
    }

    public final Date getDate() {
        return resource.getDate();
    }

    public final String getDescription() {
        return resource.getDescription();
    }

    public final Set<DiscoveredFeed> getDiscoveredFeeds() {
        return resource.getDiscoveredFeeds();
    }

    public final int getHttpStatus() {
        return resource.getHttpStatus();
    }

    public final int getId() {
        return resource.getId();
    }

    public final Date getLastChanged() {
        return resource.getLastChanged();
    }

    public final Date getLastScanned() {
        return resource.getLastScanned();
    }

    public final String getName() {
        return resource.getName();
    }

    public final SyndEntry getRssItem() {
        return resource.getRssItem();
    }

    public final Set<Tag> getTags() {
        return resource.getTags();
    }

    public final String getType() {
        return resource.getType();
    }
    
    public String getUrl() {
        return resource.getUrl();
    }

    public final void setDate(Date date) {
        resource.setDate(date);
    }

    public final void setDescription(String description) {
        resource.setDescription(description);
    }

    public final void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
        resource.setDiscoveredFeeds(discoveredFeeds);
    }

    public final void setHttpStatus(int httpStatus) {
        resource.setHttpStatus(httpStatus);
    }

    public final void setId(int id) {
        resource.setId(id);
    }

    public final void setLastChanged(Date lastChanged) {
        resource.setLastChanged(lastChanged);
    }

    public final void setLastScanned(Date lastScanned) {
        resource.setLastScanned(lastScanned);
    }

    public final void setName(String name) {
        resource.setName(name);
    }

    public final void setTags(Set<Tag> tags) {
        resource.setTags(tags);
    }

    public final void setUrl(String url) {
        resource.setUrl(url);
    }

    public final void addTag(Tag tag) {
        resource.addTag(tag);        
    }

    public Date getLiveTime() {
        return resource.getLiveTime();
    }

    public void setLiveTime(Date time) {
        resource.setLiveTime(time);        
    }

	public int getTechnoratiCount() {
		return resource.getTechnoratiCount();
	}

	public void setTechnoratiCount(int technoratiCount) {
	}

	public Geocode getGeocode() {
		return null;
	}

	public void setGeocode(Geocode geocode) {
	}
    
	public void getRemoveTag(Tag tag) {
	}

}
