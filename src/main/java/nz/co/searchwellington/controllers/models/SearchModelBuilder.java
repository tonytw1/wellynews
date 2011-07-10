package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.web.servlet.ModelAndView;

public class SearchModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	private static final String KEYWORDS_PARAMETER = "keywords";
	
	private UrlBuilder urlBuilder;
	
	public SearchModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
	}
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getParameter(KEYWORDS_PARAMETER) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		ModelAndView mv = new ModelAndView();
		final String keywords = request.getParameter(KEYWORDS_PARAMETER);
		int page = getPage(request);
		
		mv.addObject("page", page);

		Tag tag= null;
		if (request.getAttribute("tags") != null) {
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			tag = tags.get(0);
			mv.addObject("tag", tag);
		}
		
		int contentCount = 0;
		if (request.getAttribute("tags") != null) {
			contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords, tag);
		} else {
			contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords);
		}
				
		int startIndex = getStartIndex(page);
		if (startIndex > contentCount) {
			return null;
		}
		
		populatePagination(mv, startIndex, contentCount);
		
		mv.addObject("query", keywords);
		mv.addObject("heading", "Search results - " + keywords);
				
		if (tag != null) {
			mv.addObject("main_content", contentRetrievalService.getNewsitemsMatchingKeywords(keywords, tag, startIndex, MAX_NEWSITEMS));			
		} else {
			mv.addObject("main_content",  contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, MAX_NEWSITEMS));
			mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords));	// TODO should able able to this as part of the above search query?
		}
		mv.addObject("main_content_total", contentCount);
		mv.addObject("main_heading", "Matching Newsitems");
		mv.addObject("main_description", "Found " + contentCount + " matching newsitems");	// TODO Pull plural function from macro into java and use here.
		mv.addObject("description", "Search results for '" + keywords + "'");
		mv.addObject("link", urlBuilder.getSearchUrlFor(keywords));
        return mv;
	}
	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));		
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "search";
	}
	
}
