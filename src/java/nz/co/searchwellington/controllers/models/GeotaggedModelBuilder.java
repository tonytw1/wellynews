package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
	private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;
	
	public GeotaggedModelBuilder(ResourceRepository resourceDAO, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/geotagged(/(rss|json))?$");
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building geotagged page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Geotagged newsitems");        		
			mv.addObject("description", "Geotagged newsitems");
			mv.addObject("link", urlBuilder.getGeotaggedUrl());	

			// TODO pagination
			final List<Resource> geotaggedNewsitems = resourceDAO.getAllValidGeocoded(MAX_NEWSITEMS, showBroken);
			mv.addObject("main_content", geotaggedNewsitems);
			mv.addObject("geocoded", geotaggedNewsitems);
			
			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(), rssUrlBuilder.getRssUrlForGeotagged());

			
			// TODO rename
			mv.setViewName("geocoded");
			return mv;
		}
		return null;
	}
	

	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		mv.addObject("geotagged_tags", resourceDAO.getGeotaggedTags(showBroken)); 
	}

}
