package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.filters.LocationParameterFilter;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
    private static final int HOW_FAR_IS_CLOSE_IN_KILOMETERS = 2;
    
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
			
			final Geocode userSuppliedLocation = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);						
			final boolean userSuppliedALocation = userSuppliedLocation != null;
			if (userSuppliedALocation) {
				if (userSuppliedLocation.isValid()) {
					
					final double latitude = userSuppliedLocation.getLatitude();
					final double longitude = userSuppliedLocation.getLongitude();
					log.info("Location is set to: " + latitude + ", " + longitude);
					
					final int page = getPage(request);
					mv.addObject("page", page);
					final int startIndex = getStartIndex(page);
					final int totalNearbyCount = contentRetrievalService.getNewsitemsNearCount(latitude, longitude, HOW_FAR_IS_CLOSE_IN_KILOMETERS);
					if (startIndex > totalNearbyCount) {
						return null;
					}
					populatePagination(mv, startIndex, totalNearbyCount);

					
					mv.addObject("latitude", latitude);
					mv.addObject("longitude", longitude);
					mv.addObject("main_content", contentRetrievalService.getNewsitemsNear(latitude, longitude, HOW_FAR_IS_CLOSE_IN_KILOMETERS));
				
					if (userSuppliedLocation.getAddress() != null) {
						mv.addObject("heading", rssUrlBuilder.getRssTitleForGeotagged(userSuppliedLocation.getAddress()));
					} else {
						mv.addObject("heading", rssUrlBuilder.getRssTitleForGeotagged(latitude, longitude));
					}				
					setRssForLocation(mv, userSuppliedLocation);
				
				}				
				return mv;				
			}
			
			final int page = getPage(request);
			mv.addObject("page", page);	// TODO push to populate pagination.
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
		if (request.getAttribute("location") == null) {
			mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags());
		}
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "geocoded";
	}

	private void setRssForLocation(ModelAndView mv, Geocode location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		if (location.getAddress() != null) {		
			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(location.getAddress()), rssUrlBuilder.getRssUrlForGeotagged(location.getAddress()));					
		} else {
			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(latitude, longitude), rssUrlBuilder.getRssUrlForGeotagged(latitude, longitude));
		}
	}
	
}
