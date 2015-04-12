package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TagCommentModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(TagCommentModelBuilder.class);

	private static final String TAG_COMMENT = "tagComment";
	private static final String TAGS = "tags";
	
	private final ContentRetrievalService contentRetrievalService;
	private final UrlBuilder urlBuilder;
	private final RssUrlBuilder rssUrlBuilder;
    private final CommonAttributesModelBuilder commonAttributesModelBuilder;

	@Autowired
	public TagCommentModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder, CommonAttributesModelBuilder commonAttributesModelBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
		this.rssUrlBuilder = rssUrlBuilder;
        this.commonAttributesModelBuilder = commonAttributesModelBuilder;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute(TAGS);
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		boolean hasCommentPath = request.getPathInfo().matches("^(.*?)/comment(/(rss|json))?$");		
		return isSingleTagPage && hasCommentPath;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.debug("Building tag comment page model");
			List<Tag> tags = (List<Tag>) request.getAttribute(TAGS);
			Tag tag = tags.get(0);

			int page = commonAttributesModelBuilder.getPage(request);
			int startIndex = commonAttributesModelBuilder.getStartIndex(page);
			return populateTagCommentPageModelAndView(tag, startIndex);
		}
		return null;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {	
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return TAG_COMMENT;
	}
	
	private ModelAndView populateTagCommentPageModelAndView(Tag tag, int startIndex) {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName() + " comment");        		
		mv.addObject("description", tag.getDisplayName() + " comment");
		mv.addObject("link", urlBuilder.getTagCommentUrl(tag));
		
		final List<FrontendResource> allCommentedForTag = contentRetrievalService.getCommentedNewsitemsForTag(tag, CommonAttributesModelBuilder.MAX_NEWSITEMS, startIndex);
		mv.addObject("main_content", allCommentedForTag);
		
		int count = contentRetrievalService.getCommentedNewsitemsForTagCount(tag);
		mv.addObject("main_content_total", count);
		
		if (!allCommentedForTag.isEmpty()) {
			commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagComment(tag), rssUrlBuilder.getRssUrlForTagComment(tag));
		}
		mv.setViewName(TAG_COMMENT);
		return mv;
	}
	
}
