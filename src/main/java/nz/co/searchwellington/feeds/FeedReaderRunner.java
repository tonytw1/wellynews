package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.model.Feed;

import org.apache.log4j.Logger;

public class FeedReaderRunner {

	Logger log = Logger.getLogger(FeedReaderRunner.class);
	
	private FeedReader feedReader;
   
	
	public FeedReaderRunner(FeedReader feedReader) {		
		this.feedReader = feedReader;
	}


	public void readSingleFeed(Feed feed) {
		this.feedReader.processFeed(feed.getId());		
	}
	
	
	public void readAllFeeds(List<Feed> feeds) {
		log.info("Running Reader.");
		for (Feed feed: feeds) {      
			this.feedReader.processFeed(feed.getId());
		}
		log.info("Finished reading feeds.");		
	}

	
	
}
