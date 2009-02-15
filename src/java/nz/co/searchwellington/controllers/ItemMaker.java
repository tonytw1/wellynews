
package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.decoraters.CalendarFeedWrapper;
import nz.co.searchwellington.model.decoraters.FeedWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditableFeedWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditableNewsitemWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditablePublishedResourceWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditableResourceWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditableWebsiteWrapper;


public class ItemMaker {

    private SiteInformation siteInformation;
    
    
    public ItemMaker(SiteInformation siteInformation) {     
        this.siteInformation = siteInformation;
    }


    public List<? extends Resource> setEditUrls(final List<? extends Resource> resources, User loggedInUser) {
        if (loggedInUser == null) {
            return resources;
        }
        
        List <Resource> items = new ArrayList<Resource>();
        for (Resource resource : resources) {
            
            if (resource instanceof PublishedResource) {
            	if (resource instanceof Feed) {                
            		EditableFeedWrapper editableFeedWrapper = new EditableFeedWrapper((Feed) resource);                   
                    items.add(editableFeedWrapper);
            	} else if (resource instanceof Newsitem) {                  
                    items.add(new EditableNewsitemWrapper((Newsitem) resource));
                } else  { 	
            		items.add(new EditablePublishedResourceWrapper((PublishedResource) resource));
            	}
                
            } else {
              
                if (resource instanceof Website) {
                    items.add(new EditableWebsiteWrapper((Website) resource));
                } else {                
                    items.add(new EditableResourceWrapper(resource));
                }
                
            } 
        }
        return items;
    }
    
    
    public List<Resource> wrapCalendars(List<Resource> calendars) {
        List<Resource> wrappedCalendars = new ArrayList<Resource>();
        for (Resource calendarFeed : calendars) {
            wrappedCalendars.add(new CalendarFeedWrapper((CalendarFeed) calendarFeed, siteInformation));
        }
        return wrappedCalendars;
    }
    


    public List<Feed> wrapFeeds(final List<Feed> allFeeds, User loggedInUser) {
        List<Feed> wrappedFeeds = new ArrayList<Feed>();
        for (Feed resource : allFeeds) {            
            Feed wrapped = new FeedWrapper(resource, siteInformation, loggedInUser);
            wrappedFeeds.add(wrapped);
        }
        return wrappedFeeds;
    }
    
    
    
    
    /*
    @SuppressWarnings("unchecked")
    public HashMap makeItem(Resource resource, User loggedInUser) {
        HashMap item = new HashMap();
        
        
        
        
        
      
                
        if (resource.getType().equals("F")) {      
            item.put("latestItem", DateFormatter.formatDate(((Feed) resource).getLatestItemDate(), "d MMM yyyy"));
            // TODO get some hours and minutes into this field.
            item.put("lastRead", DateFormatter.formatDate(((Feed) resource).getLastRead(), "d MMM yyyy"));   
        }
        
        if (resource.getType().equals("L") || resource.getType().equals("F")) {         
            item.put("lastChanged", DateFormatter.formatDate(resource.getLastChanged(), "d MMM yyyy"));            
        }
        
       
    
        
        if (resource.getType().equals("W")) {
            item.put("newsitems", ((Website) resource).getNewsitems());
            item.put("feeds", ((Website) resource).getFeeds());
            item.put("watchlist", ((Website) resource).getWatchlist());            
        }

        
        if (resource.getType().equals("N")) {
            CommentFeed commentFeed = ((Newsitem) resource).getCommentFeed();
            // TODO is this needed?
            if (commentFeed != null) {
                item.put("commentFeed", commentFeed);
            } else {
                log.debug("Newsitem #" + resource.getId() + " has no comment feed.");                
            }
            
           item.put("comments", ((Newsitem) resource).getComments());
        }
        
        
        
        // TODO share with ResourceEditController
        final boolean userCanEditItems = loggedInUser != null;
        if (userCanEditItems) {
            item.put("httpStatus", resource.getHttpStatus());
            item.put("editUrl", "edit/edit?resource=" + resource.getId());
            item.put("deleteUrl", "edit/delete?resource=" + resource.getId());
            item.put("linkCheckUrl", "admin/linkchecker/add?resource=" + resource.getId());
            
            if (resource.getType().equals("F")) {
                item.put("acceptance", ((Feed) resource).getAcceptancePolicy());
            }
            
            if (resource.getHttpStatus() == 200) {
                item.put("status", "ok");            
            } else {
                item.put("status", "broken");
            }
        }
        
        return item;
        
    }
    */
    
}
