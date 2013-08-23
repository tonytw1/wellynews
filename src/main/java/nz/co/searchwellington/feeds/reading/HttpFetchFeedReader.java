package nz.co.searchwellington.feeds.reading;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.utils.TextTrimmer;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.utils.UrlFilters;
import nz.co.searchwellington.views.GeocodeToPlaceMapper;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Reference;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

@Component
public class HttpFetchFeedReader implements FeedItemFetcher {
	
	private final Logger log = Logger.getLogger(HttpFetchFeedReader.class);
	
    private static final int MAXIMUM_BODY_LENGTH = 400;
	
	private RssHttpFetcher rssFetcher;
    private TextTrimmer textTrimmer;
	private GeocodeToPlaceMapper geocodeToPlaceMapper;
	private UrlCleaner urlCleaner;
	
	@Autowired
	public HttpFetchFeedReader(RssHttpFetcher rssFetcher,
			TextTrimmer textTrimmer, GeocodeToPlaceMapper geocodeToPlaceMapper,
			UrlCleaner urlCleaner) {
		this.rssFetcher = rssFetcher;
		this.textTrimmer = textTrimmer;
		this.geocodeToPlaceMapper = geocodeToPlaceMapper;
		this.urlCleaner = urlCleaner;
	}

	/* (non-Javadoc)
	 * @see nz.co.searchwellington.feeds.reading.FeedItemFetcher#fetchFeedItems(nz.co.searchwellington.model.Feed)
	 */
	public List<FrontendFeedNewsitem> fetchFeedItems(Feed feed) {
		final List<FrontendFeedNewsitem> feedNewsitems = Lists.newArrayList();
		final SyndFeed syndfeed = rssFetcher.httpFetch(feed.getUrl());
        if (syndfeed != null) {
            List entires = syndfeed.getEntries();
            for (Iterator iter = entires.iterator(); iter.hasNext();) {
            	try {
            		SyndEntry item = (SyndEntry) iter.next();
            		FrontendFeedNewsitem feedItem = extractNewsitemFromFeedEntire(feed, item);                
            		feedItem.setDescription(textTrimmer.trimToCharacterCount(feedItem.getDescription(), MAXIMUM_BODY_LENGTH));	// TODO this is in the wrong place - should be an acceptance step
            		feedNewsitems.add(feedItem);
            		
            	} catch (Exception e) {
            		log.error("Unexpected exception while processing feed item", e);
				}
            }
            
        } else {
            log.warn("Feed was null after loading attempt; returning empty list.");
        }
        
        log.info("Found " + feedNewsitems.size() + " feed newsitems");
        return feedNewsitems;
	}
	
	private FrontendFeedNewsitem extractNewsitemFromFeedEntire(Feed feed, SyndEntry item) {
        final String description = getBodyFromSyndItem(item);
        
        Date itemDate = null;
        if (item.getPublishedDate() != null) {        
            itemDate = item.getPublishedDate();
        }
                
        String url = item.getLink();
        if (url != null) {        	
            url = urlCleaner.cleanSubmittedItemUrl(url);
        }
        // TODO feed decision maker and feedreader and user submissions should share the same title cleaning logic
	    return makeFeednewsitemFromSyndEntry(feed, item, description, itemDate, url);
	}
	
	private FrontendFeedNewsitem makeFeednewsitemFromSyndEntry(Feed feed, SyndEntry item, String description, Date itemDate, String url) {
		FrontendFeed frontendFeed = new FrontendFeedImpl();
		frontendFeed.setUrlWords(feed.getUrlWords());
		FrontendFeedNewsitem feedItem = new FrontendFeedNewsitem();
		feedItem.setFeed(frontendFeed);		
        feedItem.setName(item.getTitle().trim());
        feedItem.setUrl(url);
        feedItem.setDescription(description);
        feedItem.setDate(itemDate);
        feedItem.setPublisherName(feed.getPublisherName());
        feedItem.setImage(extractThumbnail(item));
        feedItem.setPlace(extractPlace(item));      
        return feedItem;
	}

	private Place extractPlace(SyndEntry item) {
		GeoRSSModule geoModule = (GeoRSSModule) GeoRSSUtils.getGeoRSS(item);
		if (geoModule != null) {
			final String address = geoModule.getPosition().getLatitude() + ", " + geoModule.getPosition().getLongitude();
			log.debug("Location is: " + address);
			return geocodeToPlaceMapper.mapGeocodeToPlace(new Geocode(address, geoModule.getPosition().getLatitude(), geoModule.getPosition().getLongitude()));
		}
		return null;
	}
	
	private FrontendImage extractThumbnail(SyndEntry item) {		
        MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
        if (mediaModule != null) {
        	log.debug("Found media module for item: " + item.getTitle());
        	
        	MediaContent[] mediaContents = mediaModule.getMediaContents();
			if (mediaContents.length > 0) {
				MediaContent mediaContent = mediaContents[0];
								
				Metadata metadata = mediaContent.getMetadata();
				Thumbnail[] thumbnails = metadata.getThumbnail();
				if (thumbnails.length > 0) {
					Thumbnail thumbnail = thumbnails[0];		
					log.info("Took first thumbnail on first mediaContent: " + thumbnail.getUrl());
					return new FrontendImage(thumbnail.getUrl().toExternalForm());
				}
				
				if (mediaContent.getReference() != null) {
					final Reference reference = mediaContent.getReference();
					if (reference.toString().endsWith(".jpg")) {
						log.info("Took image reference from first MediaContent: " + reference);						
					}
					return new FrontendImage(reference.toString());
				}
				log.info(mediaContent.getReference());
			}
						
			Thumbnail[] thumbnails = mediaModule.getMetadata().getThumbnail();
			if (thumbnails.length > 0) {					
				Thumbnail thumbnail = thumbnails[0];
				log.info("Took first thumbnail from module metadata: " + thumbnail.getUrl());
				return new FrontendImage(thumbnail.getUrl().toExternalForm());				
			}
			
        }	
		return null;
	}
	
	private String getBodyFromSyndItem(SyndEntry item) {
		String description = "";
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
