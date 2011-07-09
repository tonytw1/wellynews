package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.frontend.FrontendResource;
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

	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		ModelAndView mv = new ModelAndView();
		final String keywords = request.getParameter(KEYWORDS_PARAMETER);
		mv.addObject("query", keywords);
		mv.addObject("heading", "Search results - " + keywords);
        List<FrontendResource> newsitemsMatchingKeywords = contentRetrievalService.getNewsitemsMatchingKeywords(keywords, null);
		mv.addObject("main_content", newsitemsMatchingKeywords);
		
        mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords));	// TODO should able able to this as part of the above search query?
        
		mv.addObject("main_heading", "Matching Newsitems");
        mv.addObject("main_description", "Found " + newsitemsMatchingKeywords.size() + " matching newsitems.");
        
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
