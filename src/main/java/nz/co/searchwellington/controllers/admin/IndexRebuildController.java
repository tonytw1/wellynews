package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;

@Controller
public class IndexRebuildController {
    
	private final ElasticSearchIndexRebuildService elasticSearchIndexUpdateService;
	private final LoggedInUserFilter loggedInUserFilter;
	private final ViewFactory viewFactory;
	
	@Autowired
    public IndexRebuildController(ElasticSearchIndexRebuildService elasticSearchIndexUpdateService,
    		LoggedInUserFilter loggedInUserFilter,
    		ViewFactory viewFactory) {       
		this.elasticSearchIndexUpdateService = elasticSearchIndexUpdateService;
        this.loggedInUserFilter = loggedInUserFilter;
		this.viewFactory = viewFactory;
    }
    
    @RequestMapping("/admin/indexbuilder")
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	final User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null || !loggedInUser.isAdmin()) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	        
        boolean deleteAll = false;
		if (request.getParameter("delete") != null) {
			deleteAll = true;
		}
		
		elasticSearchIndexUpdateService.buildIndex(deleteAll);
		
		return new ModelAndView(viewFactory.getJsonView()).addObject("data", "ok");
    }
    
}
