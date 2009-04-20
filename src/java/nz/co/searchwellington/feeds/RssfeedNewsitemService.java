package nz.co.searchwellington.feeds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.rss.RssFeedDAO;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.utils.TextTrimmer;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;


public class RssfeedNewsitemService {
           
    public final Logger log = Logger.getLogger(RssfeedNewsitemService.class);
    private static final int MAXIMUM_BODY_LENGTH = 400;
    
    private UrlCleaner urlCleaner;
	private RssFeedDAO rssFeedDAO;
    

    public RssfeedNewsitemService(UrlCleaner urlCleaner, RssFeedDAO rssFeedDAO) {
    	// TODO eratic use of this urlCleaner - this service should not make tinyurl calls - only when expecting - or on loading!
        this.urlCleaner = urlCleaner;
        this.rssFeedDAO = rssFeedDAO;
    }

    
    public List<FeedNewsitem> getFeedNewsitems(Feed feed) throws IllegalArgumentException, IOException {
        List<FeedNewsitem> feedNewsitems = new ArrayList<FeedNewsitem>();

        SyndFeed syndfeed = rssFeedDAO.getFeedByUrl(feed.getUrl());
        if (syndfeed != null) {
            List entires = syndfeed.getEntries();
            int itemNumber = 1;
            for (Iterator iter = entires.iterator(); iter.hasNext();) {
                SyndEntry item = (SyndEntry) iter.next();
                FeedNewsitem feedItem = extractNewsitemFromFeedEntire(feed, item);                
                feedItem.setItemNumber(itemNumber);
                trimExcessivelyLongBodies(feedItem);
                feedNewsitems.add(feedItem);
                itemNumber++;
            }
        } else {
            log.error("Feed was null after loading attempt; returning empty list.");
        }
        return feedNewsitems;
    }


    public Date getLatestPublicationDate(Feed feed) throws IllegalArgumentException, IOException {
        Date latestPublicationDate = null;
        List<FeedNewsitem> feeditems = getFeedNewsitems(feed);
        for (Resource resource : feeditems) {
            if (resource.getDate() != null && (latestPublicationDate == null || resource.getDate().after(latestPublicationDate))) {
                latestPublicationDate = resource.getDate();           
            }
        }
        return latestPublicationDate;
    }

    
    private FeedNewsitem extractNewsitemFromFeedEntire(Feed feed, SyndEntry item) {
        String description = null;
        SyndContent descriptionContent = (SyndContent) item.getDescription();
        
        if (descriptionContent != null) {        
            description = UrlFilters.stripHtml(descriptionContent.getValue());
        }
        
        if (item.getContents().size() > 0) {
            SyndContent content = (SyndContent) item.getContents().get(0);
            description = UrlFilters.stripHtml(content.getValue());            
        } 
        
        
        Date itemDate = null;
        if (item.getPublishedDate() != null) {        
            itemDate = item.getPublishedDate();
        }
                
        String url = item.getLink();
        if (url != null) {
            url = urlCleaner.cleanSubmittedItemUrl(url);
        }
        // TODO This reference should really come from the resourceDAO.
        FeedNewsitem feedItem = new FeedNewsitem(0, item.getTitle(), url, description, itemDate, feed.getPublisher(), new HashSet<Tag>(), new HashSet<DiscoveredFeed>());
        
        log.debug("Date of loaded newsitem is: " + feedItem.getDate());
        feedItem.setFeed(feed);        
        return feedItem;
    }
    
    
    
    public Newsitem makeNewsitemFromFeedItem(FeedNewsitem feedNewsitem) {
    	Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), feedNewsitem.getDescription(), feedNewsitem.getDate(), feedNewsitem.getPublisher(), 
    			feedNewsitem.getTags(), 
    			new HashSet<DiscoveredFeed>());
    	return newsitem;
	}
    
    
    
    private void trimExcessivelyLongBodies(Resource feedItem) {
        boolean bodyIsToLong = feedItem.getDescription() != null && feedItem.getDescription().length() > MAXIMUM_BODY_LENGTH;
        if (bodyIsToLong) {
            feedItem.setDescription(TextTrimmer.trimToCharacterCount(feedItem.getDescription(), MAXIMUM_BODY_LENGTH));
        }
    }
    
    
    
}
