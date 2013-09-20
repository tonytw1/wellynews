package nz.co.searchwellington.feeds;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RssfeedNewsitemService {

	private static Logger log = Logger.getLogger(RssfeedNewsitemService.class);

	private CachingRssfeedNewsitemService cachingRssfeedNewsitemService;
	private HibernateResourceDAO resourceDAO;
	
	@Autowired
	public RssfeedNewsitemService(
			CachingRssfeedNewsitemService cachingRssfeedNewsitemService,
			HibernateResourceDAO resourceDAO,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService) {
		this.cachingRssfeedNewsitemService = cachingRssfeedNewsitemService;
		this.resourceDAO = resourceDAO;
	}

	public List<FrontendFeedNewsitem> getFeedNewsitems(Feed feed) {
		return cachingRssfeedNewsitemService.getFeedNewsitems(feed);
	}
	
	public final Date getLatestPublicationDate(Feed feed) {
		Date latestPublicationDate = null;
		List<FrontendFeedNewsitem> feeditems = getFeedNewsitems(feed);
		for (FrontendFeedNewsitem feeditem : feeditems) {
			if (feeditem.getDate() != null && (latestPublicationDate == null || feeditem.getDate().after(latestPublicationDate))) {
				latestPublicationDate = feeditem.getDate();
			}
		}
		return latestPublicationDate;
	}
	
	public FrontendFeedNewsitem getFeedNewsitemByUrl(Feed feed, String url) {
		List<FrontendFeedNewsitem> feedNewsitems = this.getFeedNewsitems(feed);
		Iterator<FrontendFeedNewsitem> i = feedNewsitems.iterator();
		while (i.hasNext()) {
			FrontendFeedNewsitem feedNewsitem = i.next();
			if (feedNewsitem.getUrl() != null && feedNewsitem.getUrl().equals(url)) {
				return feedNewsitem;
			}
		}
		return null;
	}
	
	public boolean isUrlInAcceptedFeeds(String url) {
		log.debug("Looking for url in accepted feeds: " + url);
		for(Feed feed : resourceDAO.getAllFeeds()) {
			if (feed.getAcceptancePolicy().equals("accept") || feed.getAcceptancePolicy().equals("accept_without_dates")) {
				log.debug("Checking feed: " + feed.getName());
				List <FrontendFeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
				for (FrontendFeedNewsitem feedNewsitem : feednewsItems) {
					log.debug("Checking feeditem: " + feedNewsitem.getUrl());
					if (feedNewsitem.getUrl().equals(url)) {
						log.debug("Found: " + url);
						return true;
					}
				}
			}
		}
		return false;		
	}
	
}