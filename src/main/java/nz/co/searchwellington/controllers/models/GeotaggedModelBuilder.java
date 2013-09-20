package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.filters.LocationParameterFilter;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.base.Strings;

@Component
public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
	private static final int REFINEMENTS_TO_SHOW = 8;
    protected static final double HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0;
    
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;
	private RelatedTagsService relatedTagsService;
	
	@Autowired
	public GeotaggedModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder, 
			RelatedTagsService relatedTagsService) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
		this.relatedTagsService = relatedTagsService;
	}

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/geotagged(/(rss|json))?$");
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.debug("Building geotagged page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Geotagged newsitems");
			mv.addObject("description", "Geotagged newsitems");
			mv.addObject("link", urlBuilder.getGeotaggedUrl());
			
			final Place userSuppliedPlace = (Place) request.getAttribute(LocationParameterFilter.LOCATION);	
			
			final boolean hasUserSuppliedALocation = userSuppliedPlace != null && userSuppliedPlace.getLatLong() != null;
			if (hasUserSuppliedALocation) {
				final LatLong latLong = userSuppliedPlace.getLatLong();
				log.debug("Location is set to: " + userSuppliedPlace.getLatLong());
				
				final int page = getPage(request);
				mv.addObject("page", page);
				final int startIndex = getStartIndex(page);
				
				final double radius = getLocationSearchRadius(request);					
				final long totalNearbyCount = contentRetrievalService.getNewsitemsNearCount(latLong, radius);
				if (startIndex > totalNearbyCount) {
					return null;
				}
				populatePagination(mv, startIndex, totalNearbyCount);
				
				mv.addObject("location", userSuppliedPlace);
				
				log.debug("Populating main content with newsitems near: " + latLong + " (radius: " + radius + ")");
				mv.addObject("main_content", contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, MAX_NEWSITEMS));
			
				if (!Strings.isNullOrEmpty(userSuppliedPlace.getAddress())) {
					mv.addObject("heading", rssUrlBuilder.getRssTitleForPlace(userSuppliedPlace));
				}			
				setRssUrlForLocation(mv, userSuppliedPlace);							
				return mv;				
			}
			
			final int page = getPage(request);
			mv.addObject("page", page);	// TODO push to populate pagination.
			final int startIndex = getStartIndex(page);
			final long totalGeotaggedCount = contentRetrievalService.getGeotaggedCount();
			if (startIndex > totalGeotaggedCount) {
				return null;
			}
			
			mv.addObject("main_content", contentRetrievalService.getGeocoded(startIndex, MAX_NEWSITEMS));			
			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(), rssUrlBuilder.getRssUrlForGeotagged());
			
			populatePagination(mv, startIndex, totalGeotaggedCount);
			return mv;
		}
		return null;
	}

	private double getLocationSearchRadius(HttpServletRequest request) {
		double radius = HOW_FAR_IS_CLOSE_IN_KILOMETERS;
		if (request.getAttribute(LocationParameterFilter.RADIUS) != null) {
			radius = (Double) request.getAttribute(LocationParameterFilter.RADIUS);
		}
		return radius;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		if (request.getAttribute(LocationParameterFilter.LOCATION) == null) {
			mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags());
			
		} else {
			final Place userSuppliedPlace = (Place) request.getAttribute(LocationParameterFilter.LOCATION);			
			final List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, HOW_FAR_IS_CLOSE_IN_KILOMETERS, REFINEMENTS_TO_SHOW);
			if (!relatedTagLinks.isEmpty()) {
				mv.addObject("related_tags", relatedTagLinks);
			}

			List<PublisherContentCount> relatedPublisherLinks = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, HOW_FAR_IS_CLOSE_IN_KILOMETERS, REFINEMENTS_TO_SHOW);
			if (!relatedPublisherLinks.isEmpty()) {
				mv.addObject("related_publishers", relatedPublisherLinks);
			}			
		}
		
		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "geocoded";
	}

	private void setRssUrlForLocation(ModelAndView mv, Place place) {	// TODO push to url builder - needed in content_element view
		final String rssUrlForPlace = rssUrlBuilder.getRssUrlForPlace(place);
		if (rssUrlForPlace == null) {
			return;
		}
		setRss(mv, rssUrlBuilder.getRssTitleForPlace(place), rssUrlForPlace);							
	}
	
}
