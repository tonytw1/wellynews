 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.flickr.FlickrService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.urls.UrlParameterEncoder;
import nz.co.searchwellington.views.GeocodeToPlaceMapper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TagModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(TagModelBuilder.class);

	private static final String MAIN_CONTENT = "main_content";
	private static final String PAGE = "page";
	private static final String TAG = "tag";
	private static final String TAGS = "tags";
	private static final String TAG_WATCHLIST = "tag_watchlist";
	private static final String TAG_FEEDS = "tag_feeds";
	private static final String WEBSITES = "websites";
	
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ContentRetrievalService contentRetrievalService;
	private FlickrService flickrService;
	private FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
	private GeocodeToPlaceMapper geocodeToPlaceMapper;
	
	@Autowired
	public TagModelBuilder(RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			RelatedTagsService relatedTagsService,
			RssfeedNewsitemService rssfeedNewsitemService,
			ContentRetrievalService contentRetrievalService,
			FlickrService flickrService,
			FeedItemLocalCopyDecorator feedItemLocalCopyDecorator,
			GeocodeToPlaceMapper geocodeToPlaceMapper) {
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
		this.flickrService = flickrService;
		this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator;
		this.geocodeToPlaceMapper = geocodeToPlaceMapper;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute(TAGS);
		return tags != null && tags.size() == 1;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			final List<Tag> tags = (List<Tag>) request.getAttribute(TAGS);
			final Tag tag = tags.get(0);
			int page = getPage(request);
			return populateTagPageModelAndView(tag, page);
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		final List<Tag> tags = (List<Tag>) request.getAttribute(TAGS);
		final Tag tag = tags.get(0);
		
		final List<FrontendResource> taggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES);
		mv.addObject(WEBSITES, taggedWebsites);
		
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedLinksForTag(tag, 8);
		if (!relatedTagLinks.isEmpty()) {
			mv.addObject("related_tags", relatedTagLinks);
		}
		
		List<PublisherContentCount> relatedPublisherLinks = relatedTagsService.getRelatedPublishersForTag(tag, 8);
		if (!relatedPublisherLinks.isEmpty()) {
			mv.addObject("related_publishers", relatedPublisherLinks);
		}
		
		populateCommentedTaggedNewsitems(mv, tag);
		populateRelatedFeed(mv, tag);
		populateGeocoded(mv, tag);		
		populateTagFlickrPool(mv, tag);

		mv.addObject(TAG_WATCHLIST, contentRetrievalService.getTagWatchlist(tag));		
		mv.addObject(TAG_FEEDS, contentRetrievalService.getTaggedFeeds(tag));
		
        mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getViewName(ModelAndView mv) {		
		List<Resource> mainContent = (List<Resource>) mv.getModel().get(MAIN_CONTENT);
		
		List<Resource> taggedWebsites = (List<Resource>) mv.getModel().get(WEBSITES);
		List<Resource> tagWatchlist = (List<Resource>) mv.getModel().get(TAG_WATCHLIST);
		List<Resource> tagFeeds = (List<Resource>) mv.getModel().get(TAG_FEEDS);

		final boolean hasSecondaryContent = !taggedWebsites.isEmpty() || !tagWatchlist.isEmpty() || !tagFeeds.isEmpty();		
		final boolean isOneContentType = mainContent.isEmpty() || !hasSecondaryContent;		
		final Integer page = (Integer) mv.getModel().get(PAGE);
		if (page != null && page > 0) {
			mv.addObject(PAGE, page);
			return "tagNewsArchive";

		} else if (isOneContentType) {
			return "tagOneContentType";
		}
		return "tag";	
	}
	
	private ModelAndView populateTagPageModelAndView(Tag tag, int page) {
		ModelAndView mv = new ModelAndView();				
		mv.addObject(PAGE, page);
		
		int startIndex = getStartIndex(page);
		long totalNewsitemCount = contentRetrievalService.getTaggedNewitemsCount(tag);		// TODO can you get this during the main news solr call, saving a solr round trip?
		if (startIndex > totalNewsitemCount) {
			return null;
		}
		
		mv.addObject(TAG, tag);
		
		if (tag.getGeocode() != null) {			
			mv.addObject("location", geocodeToPlaceMapper.mapGeocodeToPlace(tag.getGeocode()));			
		}
		
		mv.addObject("heading", tag.getDisplayName());        		
		mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag));
		mv.addObject("link", urlBuilder.getTagUrl(tag));	
		
		final List<FrontendResource> taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, startIndex, MAX_NEWSITEMS);		
		mv.addObject(MAIN_CONTENT, taggedNewsitems);		
		
		populatePagination(mv, startIndex, totalNewsitemCount);
		
		if (taggedNewsitems.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
		}
		
		return mv;
	}

    private void populateTagFlickrPool(ModelAndView mv, Tag tag) {
    	mv.addObject("flickr_count", flickrService.getFlickrPhotoCountFor(tag));
    	mv.addObject("escaped_flickr_group_id", UrlParameterEncoder.encode(flickrService.getPoolId()));
    }
    
    private void populateCommentedTaggedNewsitems(ModelAndView mv, Tag tag) {
        List<FrontendResource> recentCommentedNewsitems = contentRetrievalService.getRecentCommentedNewsitemsForTag(tag, MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS + 1);
        List<FrontendResource> commentedToShow;
        if (recentCommentedNewsitems.size() <= MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS) {
            commentedToShow = recentCommentedNewsitems;
            
        } else {
            commentedToShow = recentCommentedNewsitems.subList(0, MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS);
        }
        
        final int commentsCount = contentRetrievalService.getCommentedNewsitemsForTagCount(tag);
        final int moreCommentCount = commentsCount - commentedToShow.size();
        if (moreCommentCount > 0) {
        	mv.addObject("commented_newsitems_morecount", moreCommentCount);
        	mv.addObject("commented_newsitems_moreurl", urlBuilder.getTagCommentUrl(tag));
        }
                
        mv.addObject("commented_newsitems", commentedToShow);          
    }
	    
    private void populateGeocoded(ModelAndView mv, Tag tag) {
        List<FrontendResource> geocoded = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW);
        log.debug("Found " + geocoded.size() + " valid geocoded resources for tag: " + tag.getName());      
        if (geocoded.size() > 0) {
            mv.addObject("geocoded", geocoded);
        }
    }
	    
    private void populateRelatedFeed(ModelAndView mv, Tag tag) {       
        Feed relatedFeed = tag.getRelatedFeed();
        if (relatedFeed != null) {
            log.debug("Related feed is: " + relatedFeed.getName());
            mv.addObject("related_feed", relatedFeed);
            
            List<FrontendFeedNewsitem> relatedFeedItems = rssfeedNewsitemService.getFeedNewsitems(relatedFeed);            
            mv.addObject("related_feed_items", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(relatedFeedItems));
            
        } else {
            log.debug("No related feed.");
        }
    }
        
}
