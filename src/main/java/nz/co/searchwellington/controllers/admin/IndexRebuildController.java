package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ElasticSearchIndexRebuildService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexRebuildController {
    
	private final ElasticSearchIndexRebuildService elasticSearchIndexUpdateService;
	private final LoggedInUserFilter loggedInUserFilter;
	
	@Autowired
    public IndexRebuildController(ElasticSearchIndexRebuildService elasticSearchIndexUpdateService, LoggedInUserFilter loggedInUserFilter) {       
		this.elasticSearchIndexUpdateService = elasticSearchIndexUpdateService;
        this.loggedInUserFilter = loggedInUserFilter;
    }
    
    @RequestMapping("/admin/indexbuilder")
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null || !loggedInUser.isAdmin()) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	
        
        boolean deleteAll = false;
		if (request.getParameter("delete") != null) {
			deleteAll = true;
		}
        
		/*
        if (solrIndexRebuildService.buildIndex(deleteAll)) {
        	mv.addObject("message", "Created new index");
        } else {
        	mv.addObject("message", "Index rebuild failed");
        }
        */
		
		elasticSearchIndexUpdateService.buildIndex(deleteAll);
		
		final ModelAndView mv = new ModelAndView("luceneIndexBuilder");
        return mv;
    }
    
}
