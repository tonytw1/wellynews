 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.filters.GoogleSearchTermFilter;
import nz.co.searchwellington.flickr.FlickrService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class TagModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(TagModelBuilder.class);
    	
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	private ConfigRepository configDAO;
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ContentRetrievalService contentRetrievalService;
	private KeywordSearchService keywordSearchService;
	private FlickrService flickrService;
	
	public TagModelBuilder(RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			RelatedTagsService relatedTagsService,
			ConfigRepository configDAO,
			RssfeedNewsitemService rssfeedNewsitemService,
			ContentRetrievalService contentRetrievalService,
			KeywordSearchService keywordSearchService, FlickrService flickrService) {
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
		this.configDAO = configDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
		this.keywordSearchService = keywordSearchService;
		this.flickrService = flickrService;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		return tags != null && tags.size() == 1;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			int page = getPage(request);
			return populateTagPageModelAndView(tag, page, request.getPathInfo());
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);
		
		final List<FrontendResource> taggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES);
		mv.addObject("websites", taggedWebsites);
		
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedLinksForTag(tag, 8);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}
		
		List<PublisherContentCount> relatedPublisherLinks = relatedTagsService.getRelatedPublishersForTag(tag, 8);
		if (relatedPublisherLinks.size() > 0) {
			mv.addObject("related_publishers", relatedPublisherLinks);
		}
		
		populateCommentedTaggedNewsitems(mv, tag);
		mv.addObject("last_changed", contentRetrievalService.getLastLiveTimeForTag(tag));
		populateRelatedFeed(mv, tag);
		populateGeocoded(mv, tag);		
		populateTagFlickrPool(mv, tag);		
		populateTagRelatedTwitter(mv, tag);		
		populateRecentlyTwittered(mv, tag);

		mv.addObject("tag_watchlist", contentRetrievalService.getTagWatchlist(tag));		
		mv.addObject("tag_feeds", contentRetrievalService.getTaggedFeeds(tag));
		
		if (request.getAttribute(GoogleSearchTermFilter.SEARCH_TERM) != null) {
			final String searchTerm = (String) request.getAttribute(GoogleSearchTermFilter.SEARCH_TERM);
			mv.addObject("searchterm", searchTerm);
			mv.addObject("searchfacets", keywordSearchService.getKeywordSearchFacets(searchTerm, null));
		}
        mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getViewName(ModelAndView mv) {		
		List<Resource> mainContent = (List<Resource>) mv.getModel().get("main_content");
		
		List<Resource> taggedWebsites = (List<Resource>) mv.getModel().get("websites");
		List<Resource> tagWatchlist = (List<Resource>) mv.getModel().get("tag_watchlist");
		List<Resource> tagFeeds = (List<Resource>) mv.getModel().get("tag_feeds");

		final boolean hasSecondaryContent = !taggedWebsites.isEmpty() || !tagWatchlist.isEmpty() || !tagFeeds.isEmpty();		
		final boolean isOneContentType = mainContent.isEmpty() || !hasSecondaryContent;		
		Integer page = (Integer) mv.getModel().get("page");
		if (page != null && page > 0) {
			mv.addObject("page", page);
			return "tagNewsArchive";

		} else if (isOneContentType) {
			return "tagOneContentType";
		}
		return "tag";	
	}
	
	private ModelAndView populateTagPageModelAndView(Tag tag, int page, String path) {
		ModelAndView mv = new ModelAndView();				
		mv.addObject("page", page);
		
		int startIndex = getStartIndex(page);
		int totalNewsitemCount = contentRetrievalService.getTaggedNewitemsCount(tag);		// TODO can you get this during the main news solr call, saving a solr round trip?
		if (startIndex > totalNewsitemCount) {
			return null;
		}
		
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName());        		
		mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag));
		mv.addObject("link", urlBuilder.getTagUrl(tag));	
		
		final List<FrontendResource> taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, startIndex, MAX_NEWSITEMS);		
		mv.addObject("main_content", taggedNewsitems);		
		
		populatePagination(mv, startIndex, totalNewsitemCount);
		
		if (taggedNewsitems.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
		}
		
		return mv;
	}
	
	private void populateRecentlyTwittered(ModelAndView mv, Tag tag) {
		mv.addObject("recently_twittered", contentRetrievalService.getRecentedTwitteredNewsitemsForTag(2, tag));
	}
	
	private void populateTagRelatedTwitter(ModelAndView mv, Tag tag) {
		if (tag.getRelatedTwitter() != null && !tag.getRelatedTwitter().equals("")) {
			log.info("Setting related twitter to: " + tag.getRelatedTwitter());
			mv.addObject("twitterUsername", tag.getRelatedTwitter());
		}
	}
 		
    private void populateTagFlickrPool(ModelAndView mv, Tag tag) {
    	mv.addObject("flickr_count", flickrService.getFlickrPhotoCountFor(tag));
    	mv.addObject("escaped_flickr_group_id", UrlFilters.encode(configDAO.getFlickrPoolGroupId()));	// TODO double call to the config doa
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
        log.info("Found " + geocoded.size() + " valid geocoded resources for tag: " + tag.getName());      
        if (geocoded.size() > 0) {
            mv.addObject("geocoded", geocoded);
        }
    }
	    
    private void populateRelatedFeed(ModelAndView mv, Tag tag) {       
        Feed relatedFeed = tag.getRelatedFeed();
        if (relatedFeed != null) {
            log.info("Related feed is: " + relatedFeed.getName());
            mv.addObject("related_feed", relatedFeed);
            
            List<FeedNewsitem> relatedFeedItems = rssfeedNewsitemService.getFeedNewsitems(relatedFeed);            
            mv.addObject("related_feed_items", rssfeedNewsitemService.addSupressionAndLocalCopyInformation(relatedFeedItems));
            
        } else {
            log.debug("No related feed.");
        }
    }
        
}
