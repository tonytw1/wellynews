package nz.co.searchwellington.feeds;

import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FeedItemAcceptor {
	
	private static Logger log = Logger.getLogger(FeedItemAcceptor.class);

	private RssfeedNewsitemService rssfeedNewsitemService;
    private AutoTaggingService autoTagger;   
    private ContentUpdateService contentUpdateService;
    
	public FeedItemAcceptor() {
	}
	
	public FeedItemAcceptor(RssfeedNewsitemService rssfeedNewsitemService,
			AutoTaggingService autoTagger,
			ContentUpdateService contentUpdateService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.autoTagger = autoTagger;
		this.contentUpdateService = contentUpdateService;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW) 
	public void acceptFeedItem(FeedNewsitem feednewsitem) {
		log.info("Accepting: " + feednewsitem.getName() + " (" + feednewsitem.getName() + ")");
		Newsitem newsitem = rssfeedNewsitemService.makeNewsitemFromFeedItem(feednewsitem);
		if (feednewsitem.getGeocode() != null) {
			newsitem.setGeocode(new Geocode(feednewsitem.getGeocode().getAddress(), feednewsitem.getGeocode().getLatitude(), feednewsitem.getGeocode().getLongitude()));
		}
        
		flattenLoudCapsInTitle(newsitem);        
        if (newsitem.getDate() == null) {
        	log.info("Accepting a feeditem with no date; setting date to current time");            
        	newsitem.setDate(new DateTime().toDate());
        }
        newsitem.setAccepted(new DateTime().toDate());
        
        contentUpdateService.create(newsitem);
        autoTagger.autotag(newsitem);
        contentUpdateService.update(newsitem);
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
