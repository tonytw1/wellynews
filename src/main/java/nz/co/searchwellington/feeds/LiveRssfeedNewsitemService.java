package nz.co.searchwellington.feeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Image;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionRepository;
import nz.co.searchwellington.utils.TextTrimmer;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class LiveRssfeedNewsitemService extends RssfeedNewsitemService {
           
    public final Logger log = Logger.getLogger(LiveRssfeedNewsitemService.class);
    private static final int MAXIMUM_BODY_LENGTH = 400;
    
    private UrlCleaner urlCleaner;
	private RssHttpFetcher rssFetcher;
    private TextTrimmer textTrimmer;
	
    public LiveRssfeedNewsitemService(ResourceRepository resourceDAO, SupressionRepository suppressionDAO, UrlCleaner urlCleaner, RssHttpFetcher rssFetcher, TextTrimmer textTrimmer) {
    	this.resourceDAO = resourceDAO;
    	this.suppressionDAO = suppressionDAO;
		this.urlCleaner = urlCleaner;
		this.rssFetcher = rssFetcher;
		this.textTrimmer = textTrimmer;
	}

	public List<FeedNewsitem> getFeedNewsitems(Feed feed) {
        List<FeedNewsitem> feedNewsitems = new ArrayList<FeedNewsitem>();

        SyndFeed syndfeed = rssFetcher.httpFetch(feed.getUrl());
        if (syndfeed != null) {
            List entires = syndfeed.getEntries();
            int itemNumber = 1;
            for (Iterator iter = entires.iterator(); iter.hasNext();) {
                SyndEntry item = (SyndEntry) iter.next();
                FeedNewsitem feedItem = extractNewsitemFromFeedEntire(feed, item);                
                feedItem.setItemNumber(itemNumber);
                
                feedItem.setDescription(textTrimmer.trimToCharacterCount(feedItem.getDescription(), MAXIMUM_BODY_LENGTH));
                feedNewsitems.add(feedItem);
                itemNumber++;
            }
        } else {
            log.warn("Feed was null after loading attempt; returning empty list.");
        }
        return feedNewsitems;
    }
	
    private FeedNewsitem extractNewsitemFromFeedEntire(Feed feed, SyndEntry item) {
        String description = null;
        description = getBodyFromSyndItem(item, description); 
                
        Date itemDate = null;
        if (item.getPublishedDate() != null) {        
            itemDate = item.getPublishedDate();
        }
                
        String url = item.getLink();
        if (url != null) {        	
            url = urlCleaner.cleanSubmittedItemUrl(url);
        }
        
        // TODO This reference should really come from the resourceDAO.
        // TODO feed decision maker and feedreader and user submissions should share the same title cleaning logic
        FeedNewsitem feedItem = new FeedNewsitem(0, item.getTitle().trim(), url, description, itemDate, new HashSet<DiscoveredFeed>(), feed.getPublisherName());       
        feedItem.setImage(extractThumbnail(feed, item));
        feedItem.setGeocode(extractGeocode(feed, item));
      
        log.debug("Date of loaded newsitem is: " + feedItem.getDate());
        feedItem.setFeed(feed);
        return feedItem;
    }


	private Geocode extractGeocode(Feed feed, SyndEntry item) {
		GeoRSSModule geoModule = (GeoRSSModule) GeoRSSUtils.getGeoRSS(item);
		if (geoModule != null) {
			final String address = geoModule.getPosition().getLatitude() + ", " + geoModule.getPosition().getLongitude();
			log.info("Location is: " + address);
			return new Geocode(address, geoModule.getPosition().getLatitude(), geoModule.getPosition().getLongitude());
		}

		return null;
	}

	private Image extractThumbnail(Feed feed, SyndEntry item) {		
        MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
        if (mediaModule != null) {
        	
        	log.debug("Found media module for feed: " + feed.getName());
        	
        	MediaContent[] mediaContents = mediaModule.getMediaContents();
			if (mediaContents.length > 0) {
				MediaContent mediaContent = mediaContents[0];				
				Metadata metadata = mediaContent.getMetadata();
				Thumbnail[] thumbnails = metadata.getThumbnail();
				if (thumbnails.length > 0) {
					Thumbnail thumbnail = thumbnails[0];		
					log.info("Found thumbnail on first media content: " + thumbnail.getUrl());
					return new Image(thumbnail.getUrl().toExternalForm(), null);
				}
			}
						
			Thumbnail[] thumbnails = mediaModule.getMetadata().getThumbnail();
			if (thumbnails.length > 0) {					
				Thumbnail thumbnail = thumbnails[0];
				log.info("Found first thumbnail on module metadata: " + thumbnail.getUrl());
				return new Image(thumbnail.getUrl().toExternalForm(), null);				
			}						
        }	
		return null;
	}
	
	private String getBodyFromSyndItem(SyndEntry item, String description) {
		// TODO; what's going on here? - why two settings? Check if this API call has updated?
		SyndContent descriptionContent = (SyndContent) item.getDescription();
        if (descriptionContent != null) {
            description = UrlFilters.stripHtml(descriptionContent.getValue());
        }
        
        if (item.getContents().size() > 0) {
            SyndContent content = (SyndContent) item.getContents().get(0);           
            description = UrlFilters.stripHtml(content.getValue());            
        }
        
		return StringEscapeUtils.unescapeHtml(description);
	}
    
}
