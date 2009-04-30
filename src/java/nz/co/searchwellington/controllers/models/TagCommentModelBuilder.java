package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class TagCommentModelBuilder extends AbstractModelBuilder implements ModelBuilder {
		
	Logger log = Logger.getLogger(TagCommentModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;

	
	public TagCommentModelBuilder(ResourceRepository resourceDAO, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		boolean hasCommentPath = request.getPathInfo().matches("^(.*?)/comment(/rss)?$");		
		return isSingleTagPage && hasCommentPath;
	}


	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building tag comment page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			return populateTagCommentPageModelAndView(tag, showBroken);
		}
		return null;
	}
	
	
	private ModelAndView populateTagCommentPageModelAndView(Tag tag, boolean showBroken) {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName() + " comment");        		
		mv.addObject("description", tag.getDisplayName() + " comment");
		mv.addObject("link", urlBuilder.getTagCommentUrl(tag));
		
	    final List<Resource> allCommentedForTag = resourceDAO.getCommentedNewsitemsForTag(tag, showBroken, MAX_NUMBER_OF_COMMENTED_TO_SHOW);		
		mv.addObject("main_content", allCommentedForTag);
		
		if (allCommentedForTag.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTagComment(tag), rssUrlBuilder.getRssUrlForTagComment(tag));
		}
		mv.setViewName("tagComment");
		return mv;
	}
	
	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {	
	}
	
}
