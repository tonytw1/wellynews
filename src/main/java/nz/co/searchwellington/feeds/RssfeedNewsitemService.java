package nz.co.searchwellington.feeds;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SupressionDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class RssfeedNewsitemService {

	private static Logger log = Logger.getLogger(RssfeedNewsitemService.class);

	private CachingRssfeedNewsitemService cachingRssfeedNewsitemService;
	private HibernateResourceDAO resourceDAO;
	private SupressionDAO suppressionDAO;
	private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
	
	@Autowired
	public RssfeedNewsitemService(CachingRssfeedNewsitemService cachingRssfeedNewsitemService,
			HibernateResourceDAO resourceDAO, SupressionDAO suppressionDAO,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService) {
		this.cachingRssfeedNewsitemService = cachingRssfeedNewsitemService;
		this.resourceDAO = resourceDAO;
		this.suppressionDAO = suppressionDAO;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
	}
	
	public List<FeedNewsitem> getFeedNewsitems(Feed feed) {
		return cachingRssfeedNewsitemService.getFeedNewsitems(feed);
	}
	
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
	
	public Newsitem getFeedNewsitemByUrl(String url) {
		log.info("Looking for feed news items by url: " + url);
		for(Feed feed : resourceDAO.getAllFeeds()) {
			List <FeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
			for (FeedNewsitem feedNewsitem : feednewsItems) {                	
				if (feedNewsitem.getUrl().equals(url)) {
					return feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feedNewsitem);
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
			if (feedNewsitem.getUrl() != null && feedNewsitem.getUrl().equals(url)) {
				return feedNewsitem;
			}
		}
		return null;
	}
	
	// TODO This is probably a seperate class as it has nothing todo with looking through the feed item cache for items.
	public List<FrontendFeedNewsitem> addSupressionAndLocalCopyInformation(List<FeedNewsitem> feedNewsitems) {
		final List<FrontendFeedNewsitem> decoratedFeednewsitems = Lists.newArrayList();
		for (FeedNewsitem feedNewsitem : feedNewsitems) {

			FrontendFeedNewsitem frontendFeedNewsitem = new FrontendFeedNewsitem(feedNewsitem);
			if (feedNewsitem.getUrl() != null) {
				
				Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());	// TODO expensive?
				if (localCopy != null) {
					frontendFeedNewsitem.setLocalCopy(localCopy.getId());
				}
				boolean isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl());					
				frontendFeedNewsitem.setSuppressed(isSuppressed);						
			}
			decoratedFeednewsitems.add(frontendFeedNewsitem);
		}
		return decoratedFeednewsitems;
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