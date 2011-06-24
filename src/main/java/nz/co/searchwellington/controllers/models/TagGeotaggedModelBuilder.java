package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class TagGeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	static Logger log = Logger.getLogger(TagGeotaggedModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;

	public TagGeotaggedModelBuilder(
			ContentRetrievalService contentRetrievalService,
			UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		boolean hasCommentPath = request.getPathInfo().matches("^(.*?)/geotagged(/(rss|json))?$");		
		return isSingleTagPage && hasCommentPath;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building tag geotagged page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			return populateTagCommentPageModelAndView(tag, showBroken);
		}
		return null;
	}
	
		
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {	
	}


	@Override
	public String getViewName(ModelAndView mv) {
		return "geotagged";
	}
	

	private ModelAndView populateTagCommentPageModelAndView(Tag tag, boolean showBroken) {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName() + " geotagged");        		
		mv.addObject("description", "Geotagged " + tag.getDisplayName() + " newsitems");
		mv.addObject("link", urlBuilder.getTagCommentUrl(tag));		
	    final List<FrontendResource> allGeotaggedForTag = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW);
		mv.addObject("main_content", allGeotaggedForTag);
		if (allGeotaggedForTag.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag));
		}		
		return mv;
	}
			
}
