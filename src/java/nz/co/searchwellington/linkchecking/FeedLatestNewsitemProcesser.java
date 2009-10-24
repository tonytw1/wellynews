package nz.co.searchwellington.linkchecking;

import java.util.Date;

import org.apache.log4j.Logger;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;


// TODO not really sure this belongs under linkchecking - feed reader should do this.
public class FeedLatestNewsitemProcesser implements LinkCheckerProcessor {
	
	private static Logger log = Logger.getLogger(FeedLatestNewsitemProcesser.class);
	
	private RssfeedNewsitemService rssfeedNewsitemService;

	    
    public FeedLatestNewsitemProcesser(RssfeedNewsitemService rssfeedNewsitemService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}
   	
	@Override
	public void process(Resource checkResource, String pageContent) {
		if (checkResource.getType().equals("F")) {
			Date latestPublicationDate = rssfeedNewsitemService.getLatestPublicationDate((Feed) checkResource);     
			((Feed) checkResource).setLatestItemDate(latestPublicationDate);
			log.debug("Latest item publication date for this feed was: " + ((Feed) checkResource).getLatestItemDate());
		}
	}
		
}

