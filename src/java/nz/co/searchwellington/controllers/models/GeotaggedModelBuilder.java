package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
		
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;
	
	public GeotaggedModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/geotagged(/(rss|json))?$");
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building geotagged page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Geotagged newsitems");        		
			mv.addObject("description", "Geotagged newsitems");
			mv.addObject("link", urlBuilder.getGeotaggedUrl());
			
			// TODO format check and push to the attribute filter
			Long latitude = null;
			if (request.getParameter(LATITUDE) != null) {
				latitude = Long.parseLong(request.getParameter(LATITUDE));
			}
			Long longitude = null;
			if (request.getParameter(LONGITUDE) != null) {
				longitude = Long.parseLong(request.getParameter(LONGITUDE));
			}
						
			final boolean isLocationSet = latitude != null && longitude != null;
			if (isLocationSet) {
				log.info("Location is set to: " + latitude + ", " + longitude);
				mv.addObject("main_content", contentRetrievalService.getGeotaggedNewsitemsNear(latitude, longitude));
				mv.addObject("heading", "Geotagged newsitems near " + latitude + ", " + longitude);
				// TODO Rss feed
				return mv;
			}
			
			final int page = getPage(request);
			mv.addObject("page", page);
			final int startIndex = getStartIndex(page);
			final int totalGeotaggedCount = contentRetrievalService.getGeotaggedCount();
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
	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags());		
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "geocoded";
	}

}
