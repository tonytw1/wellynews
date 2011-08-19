package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class TagCommentModelBuilder extends AbstractModelBuilder implements ModelBuilder {
		
	static Logger log = Logger.getLogger(TagCommentModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	private RssUrlBuilder rssUrlBuilder;

		
	public TagCommentModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder) {		
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		boolean hasCommentPath = request.getPathInfo().matches("^(.*?)/comment(/(rss|json))?$");		
		return isSingleTagPage && hasCommentPath;
	}


	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.info("Building tag comment page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);

			int page = getPage(request);
			int startIndex = getStartIndex(page);
			return populateTagCommentPageModelAndView(tag, startIndex);
		}
		return null;
	}
	
		
	@Override
	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {	
	}


	@Override
	public String getViewName(ModelAndView mv) {
		return "tagComment";
	}
	
	
	private ModelAndView populateTagCommentPageModelAndView(Tag tag, int startIndex) {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName() + " comment");        		
		mv.addObject("description", tag.getDisplayName() + " comment");
		mv.addObject("link", urlBuilder.getTagCommentUrl(tag));
		
		final List<FrontendResource> allCommentedForTag = contentRetrievalService.getCommentedNewsitemsForTag(tag, MAX_NEWSITEMS, startIndex);	   
		mv.addObject("main_content", allCommentedForTag);
		
		int count = contentRetrievalService.getCommentedNewsitemsForTagCount(tag);
		mv.addObject("main_content_total", count);
		
		if (allCommentedForTag.size() > 0) {
			setRss(mv, rssUrlBuilder.getRssTitleForTagComment(tag), rssUrlBuilder.getRssUrlForTagComment(tag));
		}
		mv.setViewName("tagComment");
		return mv;
	}
	
}
