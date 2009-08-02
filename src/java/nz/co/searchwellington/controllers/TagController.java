package nz.co.searchwellington.controllers;


import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class TagController extends BaseMultiActionController {
   
    private LoggedInUserFilter loggedInUserFilter;   
    private ContentModelBuilderService contentModelBuilder;


    public TagController(ResourceRepository resourceDAO,    	
    		LoggedInUserFilter loggedInUserFilter,
    		UrlStack urlStack,  		   		
    		ContentModelBuilderService contentModelBuilder
    		) {
        this.resourceDAO = resourceDAO;      
        this.loggedInUserFilter = loggedInUserFilter;
        this.urlStack = urlStack;               
        this.contentModelBuilder = contentModelBuilder;        
    }
    
    
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        logger.info("Starting normal content");                                  
        loggedInUserFilter.loadLoggedInUser(request);
        boolean showBroken = false;
        
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {
			urlStack.setUrlStack(request);
			addCommonModelElements(mv, showBroken);
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }

	
    private void addCommonModelElements(ModelAndView mv, boolean showBroken) throws IOException {
		mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());		
        final List<Resource> latestNewsitems = resourceDAO.getLatestNewsitems(5, showBroken);
        mv.addObject("latest_newsitems", latestNewsitems);
	}
    
}
