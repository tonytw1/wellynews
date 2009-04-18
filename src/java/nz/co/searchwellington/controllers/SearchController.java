package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;



public class SearchController extends BaseMultiActionController {

		
    Logger log = Logger.getLogger(SearchController.class);
    RequestFilter requestFilter;
	
    public SearchController(ResourceRepository resourceDAO, UrlStack urlStack, RequestFilter requestFilter, ConfigRepository configDAO, LoggedInUserFilter loggedInUserFilter) {    
		this.resourceDAO = resourceDAO;     
        this.urlStack = urlStack;
        this.requestFilter = requestFilter;
        this.configDAO = configDAO;
        this.loggedInUserFilter = loggedInUserFilter;
	}

    @SuppressWarnings("unchecked")
	public ModelAndView search(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
        ModelAndView mv = new ModelAndView();
                        
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        boolean showBroken = loggedInUser != null;
        populateAds(request, mv, showBroken);
     
        String keywords = null;
        if (request.getParameter("keywords") != null) {
            keywords = request.getParameter("keywords");
        }

        final boolean nokeywordsGiven = (keywords == null) || ("".equals(keywords));
        if (!nokeywordsGiven) {
                urlStack.setUrlStack(request);
                mv.getModel().put("heading", "Search Results");
                
                
                final List<Tag> matchingTags = resourceDAO.getTagsMatchingKeywords(keywords);
                log.info("Found " + matchingTags.size() + " matching tags for keywords '" + keywords + "'");                
                for (Tag tag : matchingTags) {
                    log.info("Found tag: " + tag.getDisplayName());
                }
                
                mv.getModel().put("matching_tags", matchingTags);
                
                final List<Resource> matchingSites = resourceDAO.getWebsitesMatchingKeywords(keywords, showBroken);
                final List<Resource> matchingNewsitems = resourceDAO.getNewsitemsMatchingKeywords(keywords, showBroken);
                                                
                if (matchingSites.size() ==0 || matchingNewsitems.size() == 0) {
                    // TODO what do you done if there are no matches for a search?
                }
                // TODO this should be an if/else group.                
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
           
            populateSecondaryLatestNewsitems(mv, loggedInUser);
          
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
