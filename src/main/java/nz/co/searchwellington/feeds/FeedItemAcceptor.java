package nz.co.searchwellington.feeds;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
public class FeedItemAcceptor {
	
	private static Logger log = Logger.getLogger(FeedItemAcceptor.class);
	
	public Newsitem acceptFeedItem(User user, Newsitem newsitem) {
		log.info("Accepting: " + newsitem.getName() + " from " + newsitem.getFeed().getName());
		flattenLoudCapsInTitle(newsitem);     
        if (newsitem.getDate() == null) {
        	log.info("Accepting a feeditem with no date; setting date to current time");            
        	newsitem.setDate(new DateTime().toDate());
        }
        
        newsitem.setAccepted(new DateTime().toDate());
        newsitem.setAcceptedBy(user);
        newsitem.setOwner(user);
        return newsitem;
    }
	
    // TODO Push to service for testing.
	private void flattenLoudCapsInTitle(Resource resource) {		
		String flattenedTitle = UrlFilters.lowerCappedSentence(resource.getName());           
        if (!flattenedTitle.equals(resource.getName())) {
        	resource.setName(flattenedTitle);
            log.info("Flatten capitalised sentence to '" + flattenedTitle + "'");
        }
	}
	
}
