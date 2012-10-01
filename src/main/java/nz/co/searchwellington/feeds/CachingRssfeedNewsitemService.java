package nz.co.searchwellington.feeds;

import java.util.Collections;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SupressionDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cachingRssfeedNewsitemService")
public class CachingRssfeedNewsitemService extends RssfeedNewsitemService {

	private FeedNewsitemCache feedNewsitemCache;
	
	@Autowired
	public CachingRssfeedNewsitemService(HibernateResourceDAO resourceDAO, SupressionDAO suppressionDAO, FeedNewsitemCache feedNewsitemCache, FeednewsItemToNewsitemService feednewsItemToNewsitemService) {
		this.resourceDAO = resourceDAO;
		this.suppressionDAO = suppressionDAO;
		this.feedNewsitemCache = feedNewsitemCache;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
	}

	@Override
	public List<FeedNewsitem> getFeedNewsitems(Feed feed) {
		List<FeedNewsitem> cachedItems = feedNewsitemCache.getFeeditems(feed.getUrl());
		if (cachedItems != null) {
			return cachedItems;
		}
		return Collections.emptyList();
	}
	
}
