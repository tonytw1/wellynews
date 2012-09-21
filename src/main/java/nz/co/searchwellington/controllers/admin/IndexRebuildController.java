package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.SolrIndexRebuildService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexRebuildController extends BaseMultiActionController {
    
	private SolrIndexRebuildService solrIndexRebuildService;
	
    public IndexRebuildController(SolrIndexRebuildService solrIndexRebuildService, LoggedInUserFilter loggedInUserFilter) {       
        this.solrIndexRebuildService = solrIndexRebuildService;
        this.loggedInUserFilter = loggedInUserFilter;
    }
    
    @RequestMapping("/admin/indexbuilder")
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null || !loggedInUser.isAdmin()) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	
        ModelAndView mv = new ModelAndView();                
        mv.setViewName("luceneIndexBuilder");
                
        boolean deleteAll = false;
		if (request.getParameter("delete") != null) {
			deleteAll = true;
		}
        
        if (solrIndexRebuildService.buildIndex(deleteAll)) {
        	mv.addObject("message", "Created new index");
        } else {
        	mv.addObject("message", "Index rebuild failed");
        }
        return mv;
    }
    
}
