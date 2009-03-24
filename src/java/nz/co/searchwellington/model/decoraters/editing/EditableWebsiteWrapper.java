package nz.co.searchwellington.model.decoraters.editing;

import java.util.Set;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;

public class EditableWebsiteWrapper extends EditableResourceWrapper implements Website {
    
    Website resource;
    
    public EditableWebsiteWrapper(Website resource) {
        super(resource);
        this.resource = resource;
    }


    public Set<Feed> getFeeds() {
        return resource.getFeeds();
    }

    public Set<Resource> getNewsitems() {
        return resource.getNewsitems();
    }

    public Set<Watchlist> getWatchlist() {
        return resource.getWatchlist();
    }

    public void setCalendars(Set<CalendarFeed> calendars) {
        // TODO Auto-generated method stub        
    }

    public Set<CalendarFeed> getCalendars() {
        // TODO Auto-generated method stub
        return null;
    }


	public String getUrlWords() {
		return null;
	}
   
    
    
    

}
