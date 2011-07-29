package nz.co.searchwellington.feeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionRepository;

import org.apache.log4j.Logger;

public abstract class RssfeedNewsitemService {

	private static Logger log = Logger.getLogger(RssfeedNewsitemService.class);

	public abstract List<FeedNewsitem> getFeedNewsitems(Feed feed);

	protected ResourceRepository resourceDAO;
	protected SupressionRepository suppressionDAO;
	
	public final Date getLatestPublicationDate(Feed feed) {
		Date latestPublicationDate = null;
		List<FeedNewsitem> feeditems = getFeedNewsitems(feed);
		for (FeedNewsitem feeditem : feeditems) {
			if (feeditem.getDate() != null && (latestPublicationDate == null || feeditem.getDate().after(latestPublicationDate))) {
				latestPublicationDate = feeditem.getDate();           
			}
		}
		return latestPublicationDate;
	}
	
	// TODO merge with addSuppressAndLocalCopyInformation?
	public Newsitem makeNewsitemFromFeedItem(Feed feed, FeedNewsitem feedNewsitem) {
		// TODO why are we newing up an instance of our superclass?
	    String description =  feedNewsitem.getDescription() != null ? feedNewsitem.getDescription() : ""; 
		Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), description, feedNewsitem.getDate(), feed.getPublisher(),
	    		new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
	    newsitem.setImage(feedNewsitem.getImage());
	    newsitem.setFeed(feed);
	    newsitem.setPublisher(feed.getPublisher());	    
	    return newsitem;
	}
	
	public Newsitem getFeedNewsitemByUrl(String url) {
		for(Feed feed : resourceDAO.getAllFeeds()) {
			List <FeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
			for (FeedNewsitem feedNewsitem : feednewsItems) {                	
				if (feedNewsitem.getUrl().equals(url)) {
					makeNewsitemFromFeedItem(feed, feedNewsitem);
				}
			}
		}
		return null;
	}
	
	public FeedNewsitem getFeedNewsitemByUrl(Feed feed, String url) {
		List<FeedNewsitem> feedNewsitems = this.getFeedNewsitems(feed);
		Iterator<FeedNewsitem> i = feedNewsitems.iterator();
		while (i.hasNext()) {
			FeedNewsitem feedNewsitem = i.next();
			if (feedNewsitem.getUrl().equals(url)) {
				return feedNewsitem;
			}
		}
		return null;
	}
	
	public List<FrontendFeedNewsitem> addSupressionAndLocalCopyInformation(List<FeedNewsitem> feedNewsitems) {
		List<FrontendFeedNewsitem> decoratedFeednewsitems = new ArrayList<FrontendFeedNewsitem>();
		for (FeedNewsitem feedNewsitem : feedNewsitems) {

			FrontendFeedNewsitem frontendFeedNewsitem = new FrontendFeedNewsitem(feedNewsitem);
			if (feedNewsitem.getUrl() != null) {
				
				Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());	// TODO expensive?
				if (localCopy != null) {
					frontendFeedNewsitem.setHasLocalCopy(true);
				}
				boolean isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl());					
				frontendFeedNewsitem.setSuppressed(isSuppressed);						
			}
			decoratedFeednewsitems.add(frontendFeedNewsitem);
		}
		return decoratedFeednewsitems;
	}
	
	// TODO In the wrong class - it only used in feed model builder
	public List<FrontendFeedNewsitem> extractGeotaggedFeeditems(List<FrontendFeedNewsitem> feedNewsitems) {
		List<FrontendFeedNewsitem> geotaggedFeedNewsitems = new ArrayList<FrontendFeedNewsitem>();
		for (FrontendFeedNewsitem feedNewsitem : feedNewsitems) {
			if (feedNewsitem.getGeocode() != null && feedNewsitem.getGeocode().isValid()) {
				geotaggedFeedNewsitems.add(feedNewsitem);
			}
		}
		if (!geotaggedFeedNewsitems.isEmpty()) {
			return geotaggedFeedNewsitems;
		}
		return null;
	}
	
	public boolean isUrlInAcceptedFeeds(String url) {
		log.info("Looking for url in accepted feeds: " + url);
		for(Feed feed : resourceDAO.getAllFeeds()) {
			if (feed.getAcceptancePolicy().equals("accept") || feed.getAcceptancePolicy().equals("accept_without_dates")) {
				log.debug("Checking feed: " + feed.getName());
				List <FeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
				for (FeedNewsitem feedNewsitem : feednewsItems) {
					log.debug("Checking feeditem: " + feedNewsitem.getUrl());
					if (feedNewsitem.getUrl().equals(url)) {
						log.info("Found: " + url);
						return true;
					}
				}
			}
		}
		return false;		
	}
	
}