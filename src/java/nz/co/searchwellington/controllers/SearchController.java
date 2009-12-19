package nz.co.searchwellington.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;



public class SearchController extends BaseMultiActionController {
	
	Logger log = Logger.getLogger(SearchController.class);
    
	private RequestFilter requestFilter;
	private ShowBrokenDecisionService showBrokenDecisionService;
	private KeywordSearchService keywordSearchService;
	
    public SearchController(ResourceRepository resourceDAO, UrlStack urlStack, RequestFilter requestFilter, ConfigRepository configDAO, ShowBrokenDecisionService showBrokenDecisionService, KeywordSearchService keywordSearchService) {    
		this.resourceDAO = resourceDAO;     
        this.urlStack = urlStack;
        this.requestFilter = requestFilter;
        this.configDAO = configDAO;
        this.showBrokenDecisionService = showBrokenDecisionService;
        this.keywordSearchService = keywordSearchService;
	}

    @SuppressWarnings("unchecked")
	public ModelAndView search(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();        
        this.populateCommonLocal(mv);
        
        requestFilter.loadAttributesOntoRequest(request);	// TODO should be on filter
        System.out.println(request.getAttribute("tag"));

                
        boolean showBroken = showBrokenDecisionService.shouldShowBroken();
        populateAds(request, mv, showBroken);
     
        String keywords = null;
        if (request.getParameter("keywords") != null) {
            keywords = request.getParameter("keywords");
            mv.addObject("query", StringEscapeUtils.escapeHtml(keywords));
        }
        
        Tag tag = null;
        if (request.getAttribute("tag") != null) {
        	tag = (Tag) request.getAttribute("tag");
        	mv.addObject("tag", tag);
        }

        final boolean nokeywordsGiven = (keywords == null) || ("".equals(keywords));
        if (!nokeywordsGiven) {
                urlStack.setUrlStack(request);
                mv.getModel().put("heading", "Search Results");
                
                mv.addObject("related_tags", keywordSearchService.getKeywordSearchFacets(keywords, showBroken, null));
                
                final List<Resource> matchingSites = keywordSearchService.getWebsitesMatchingKeywords(keywords, showBroken, tag);
                final List<Resource> matchingNewsitems = keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBroken, tag);
                                                
                if (matchingSites.size() ==0 || matchingNewsitems.size() == 0) {
                    // TODO what do you done if there are no matches for a search?
                }
                // TODO this should be in the view                
                if (matchingSites.size() >= matchingNewsitems.size()) {
                    mv.getModel().put("main_heading", "Matching Sites");
                    
                    mv.getModel().put("main_description", "Found " + matchingSites.size() + " matching sites.");
                    
                    mv.getModel().put("main_content", matchingSites);
                    mv.getModel().put("secondary_heading", "Matching Newsitems");
                    mv.getModel().put("secondary_content", matchingNewsitems);
                    
                } else {
                    mv.getModel().put("secondary_heading", "Matching Sites");
                    
                    mv.getModel().put("main_description", "Found " + matchingNewsitems.size() + " matching newsitems.");
                    mv.getModel().put("secondary_content", matchingSites);
                    mv.getModel().put("main_heading", "Matching Newsitems");
                    mv.getModel().put("main_content", matchingNewsitems);                    
                }
         
            mv.getModel().put("search_keywords", keywords);
           
            populateSecondaryLatestNewsitems(mv, showBroken);
          
            if (matchingSites.size() ==0 || matchingNewsitems.size() == 0) {
                log.debug("Using single column layout.");
                mv.setViewName("searchOneType");
            } else {
                log.debug("Using two column layout.");
                mv.setViewName("search");
            }
            
            
        } else {
            // Clearing the model keeps objects off the url.
            mv.getModel().clear();            
            String url = urlStack.getExitUrlFromStack(request);
            mv.setView(new RedirectView(url));
        }
        
        return mv;
	}

    
}
