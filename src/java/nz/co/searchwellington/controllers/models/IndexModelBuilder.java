package nz.co.searchwellington.controllers.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class IndexModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	private static final int NUMBER_OF_COMMENTED_TO_SHOW = 2;
	
	private static Logger log = Logger.getLogger(IndexModelBuilder.class);
	
	ContentRetrievalService contentRetrievalService;
	RssUrlBuilder rssUrlBuilder;
	LoggedInUserFilter loggedInUserFilter;
	UrlBuilder urlBuilder;
	
	public IndexModelBuilder(ContentRetrievalService contentRetrievalService, RssUrlBuilder rssUrlBuilder, LoggedInUserFilter loggedInUserFilter, UrlBuilder urlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.loggedInUserFilter = loggedInUserFilter;
		this.urlBuilder = urlBuilder;
	}

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/index$");
	}

	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building index page model");
			ModelAndView mv = new ModelAndView();		
			final List<Resource> latestNewsitems = contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS);                
			mv.addObject("main_content", latestNewsitems);
			
			List<ArchiveLink> archiveMonths = contentRetrievalService.getArchiveMonths();
			mv.addObject("archive_links", archiveMonths);
			if (monthOfLastItem(latestNewsitems) != null) {
				mv.addObject("main_content_moreurl", makeArchiveUrl(monthOfLastItem(latestNewsitems),  archiveMonths));
			}
			
			setRss(mv, rssUrlBuilder.getBaseRssTitke(), rssUrlBuilder.getBaseRssUrl());
			return mv;
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {		
		populateCommentedNewsitems(mv);
		populateSecondaryJustin(mv);
		populateGeocoded(mv);
		populateFeatured(mv);
		populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser());		
		populateArchiveStatistics(mv);
	}
	
	
	
	private void populateArchiveStatistics(ModelAndView mv) {
		Map<String, Integer> archiveStatistics = contentRetrievalService.getArchiveStatistics();
		if (archiveStatistics != null) {
			mv.addObject("site_count",  archiveStatistics.get("W"));
			mv.addObject("newsitem_count",  archiveStatistics.get("N"));
			mv.addObject("feed_count", archiveStatistics.get("F"));
		}
	}

	private Date monthOfLastItem(List<Resource> latestNewsitems) {
		if (latestNewsitems.size() > 0) {
			Resource lastNewsitem = latestNewsitems
					.get(latestNewsitems.size() - 1);
			if (lastNewsitem.getDate() != null) {
				Date lastDate = lastNewsitem.getDate();
				return lastDate;
			}
		}
		return null;
	}

	
	 public String makeArchiveUrl(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {    	
	    	ArchiveLink archiveLink = getArchiveLinkForDate(dateOfLastNewsitem, archiveMonths);
	    	if (archiveLink != null) {
	    		return urlBuilder.getArchiveLinkUrl(archiveLink);
	    	}
	    	return null;
	 }
	 
	 
	    
	private ArchiveLink getArchiveLinkForDate(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {
		for (ArchiveLink monthLink : archiveMonths) {
			boolean monthMatches = monthLink.getMonth().getMonth() == dateOfLastNewsitem
					.getMonth() && monthLink.getMonth().getYear() == dateOfLastNewsitem.getYear();
			if (monthMatches) {
				return monthLink;
			}
		}
		return null;
	}
	
	
	private void populateUserOwnedResources(ModelAndView mv, User loggedInUser) {
		 if (loggedInUser != null) {
			 mv.addObject("owned", contentRetrievalService.getOwnedBy(loggedInUser, 4));
		 }
	 }

	private void populateFeatured(ModelAndView mv) {
        mv.addObject("featured", contentRetrievalService.getFeaturedSites());
    }
	
	// TODO is this doing the minimal amount of hydration?
	private void populateCommentedNewsitems(ModelAndView mv) {
		final List<Resource> recentCommentedNewsitems = contentRetrievalService.getCommentedNewsitems(MAX_NUMBER_OF_COMMENTED_TO_SHOW, 0);
		if (recentCommentedNewsitems.size() <= NUMBER_OF_COMMENTED_TO_SHOW) {
			mv.addObject("commented_newsitems", recentCommentedNewsitems);
		} else {
			mv.addObject("commented_newsitems", recentCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW));
		}
		mv.addObject("commented_newsitems_moreurl", "comment");
	}
	
	
	private void populateGeocoded(ModelAndView mv) {
        List<Resource> geocoded = contentRetrievalService.getGeocoded(MAX_NUMBER_OF_GEOTAGGED_TO_SHOW);
        if (geocoded.size() > 0) {
            mv.addObject("geocoded", geocoded);
        }
    }
	
	private void populateSecondaryJustin(ModelAndView mv) {
		mv.addObject("secondary_heading", "Just In");
		mv.addObject("secondary_description", "New additions.");
		mv.addObject("secondary_content", contentRetrievalService.getLatestWebsites(4));   
		mv.addObject("secondary_content_moreurl", "justin");        
	}
	
}
