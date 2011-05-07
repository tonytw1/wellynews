package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
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
			
			int page = getPage(request);
			mv.addObject("page", page);
			final int startIndex = getStartIndex(page);
			final int totalGeotaggedCount = contentRetrievalService.getGeotaggedCount();
			if (startIndex > totalGeotaggedCount) {
				return null;
			}
			
			final List<Resource> geotaggedNewsitems = contentRetrievalService.getGeocoded(startIndex, MAX_NEWSITEMS);
			mv.addObject("main_content", geotaggedNewsitems);			
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
