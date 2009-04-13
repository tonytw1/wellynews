package nz.co.searchwellington.controllers.models;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

public class TagGeotaggedModelBuilder implements ModelBuilder {
	
	private static final int MAX_NUMBER_OF_GEOTAGGED_TO_SHOW = 30;
	
	Logger log = Logger.getLogger(TagGeotaggedModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;

	
	public TagGeotaggedModelBuilder(ResourceRepository resourceDAO, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		boolean hasCommentPath = request.getPathInfo().matches("^(.*?)/geotagged(/rss)?$");		
		return isSingleTagPage && hasCommentPath;
	}


	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) throws IOException, CorruptIndexException, FeedException {
		if (isValid(request)) {
			log.info("Building tag geotagged page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			return populateTagCommentPageModelAndView(tag, showBroken);
		}
		return null;
	}
	
	
	private ModelAndView populateTagCommentPageModelAndView(Tag tag, boolean showBroken) throws IOException, CorruptIndexException, FeedException {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName() + " geotagged");        		
		mv.addObject("description", tag.getDisplayName() + " geotagged");
		mv.addObject("link", urlBuilder.getTagCommentUrl(tag));		
	    final List<Resource> allGeotaggedForTag = resourceDAO.getAllValidGeocodedForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, showBroken);
		mv.addObject("main_content", allGeotaggedForTag);
		if (allGeotaggedForTag.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag));
		}		
		mv.setViewName("geotagged");
		return mv;
	}
	
	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {	
	}
	
	private void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);	
	}  
	
	
}
