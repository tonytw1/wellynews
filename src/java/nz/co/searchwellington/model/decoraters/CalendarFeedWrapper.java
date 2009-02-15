package nz.co.searchwellington.model.decoraters;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Website;

public class CalendarFeedWrapper extends ResourceWrapper implements PublishedResource {
    
    private SiteInformation siteInformation;
    CalendarFeed resource;
    
    
    public CalendarFeedWrapper(CalendarFeed resource, SiteInformation siteInformation) {
        super(resource);
        this.resource = resource;
        this.siteInformation = siteInformation;
    }

    @Override
    public String getUrl() {
        return siteInformation.getUrl() + "/viewcalendar?calendarfeed=" + getId();
    }

    public Website getPublisher() {
        return resource.getPublisher();
    }

    public void setPublisher(Website publisher) {
        resource.setPublisher(publisher);        
    }
    
    
    
    
    

}
