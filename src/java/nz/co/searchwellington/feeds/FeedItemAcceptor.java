
package nz.co.searchwellington.feeds;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FeedItemAcceptor {
	
	static Logger log = Logger.getLogger(FeedItemAcceptor.class);

	private RssfeedNewsitemService rssfeedNewsitemService;
    private ContentUpdateService contentUpdateService;

            
	public FeedItemAcceptor() {
	}


	public FeedItemAcceptor(RssfeedNewsitemService rssfeedNewsitemService,
			ContentUpdateService contentUpdateService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentUpdateService = contentUpdateService;
	}


	@Transactional(propagation = Propagation.REQUIRES_NEW) 
	public void acceptFeedItem(FeedNewsitem feednewsitem, Feed feed) {
		log.info("Accepting: " + feednewsitem.getName());
		Newsitem newsitem = rssfeedNewsitemService.makeNewsitemFromFeedItem(feednewsitem, feed);
        flattenLoudCapsInTitle(newsitem);
        
        if (newsitem.getDate() == null) {
        	log.info("Accepting a feeditem with no date; setting date to current time");            
        	newsitem.setDate(new DateTime().toDate());
        }
        
        contentUpdateService.update(newsitem, true);
    }

    
	private void flattenLoudCapsInTitle(Resource resource) {
		String flattenedTitle = UrlFilters.lowerCappedSentence(resource.getName());           
        if (!flattenedTitle.equals(resource.getName())) {
        	resource.setName(flattenedTitle);
            log.info("Flatten capitalised sentence to '" + flattenedTitle + "'");
        }
	}
}
