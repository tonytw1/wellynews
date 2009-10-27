package nz.co.searchwellington.feeds;

import java.util.List;
import java.util.Set;

import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class FeedReaderRunner {

	Logger log = Logger.getLogger(FeedReaderRunner.class);
	
	private ResourceRepository resourceDAO;
	private FeedReader feedReader;
    private LinkCheckerQueue linkCheckerQueue;
    private Notifier notifier;

	    
	protected FeedReaderRunner(ResourceRepository resourceDAO, FeedReader feedReader, LinkCheckerQueue linkCheckerQueue, Notifier notifier) {
		this.resourceDAO = resourceDAO;
		this.feedReader = feedReader;
		this.linkCheckerQueue = linkCheckerQueue;
		this.notifier = notifier;
	}

	
	public void readSingleFeed(Feed feed) {
		 Set<Integer> acceptedNewsitemIds = this.feedReader.processFeed(feed.getId());
		 queueAcceptedNewsitemsIds(acceptedNewsitemIds);
	}
	
	
	public void readAllFeeds(List<Feed> feeds) {
		log.info("Running Reader.");
		for (Feed feed: feeds) {      
			 queueAcceptedNewsitemsIds(this.feedReader.processFeed(feed.getId()));
		}
		log.info("Finished reading feeds.");		
	}

	
	private void queueAcceptedNewsitemsIds(Set<Integer> acceptedNewsitemIds) {
		for (Integer id : acceptedNewsitemIds) {
			log.info("Queuing for link check: " + id);
			linkCheckerQueue.add(id);
			notifier.sendAcceptanceNotification("Accepted newsitem from feed", resourceDAO.loadResourceById(id));
		}
	}
	
}
