package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.SolrIndexRebuildService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class IndexRebuildController extends BaseMultiActionController {

	static Logger log = Logger.getLogger(IndexRebuildController.class);
    
	private SolrIndexRebuildService solrIndexRebuildService;
	
    public IndexRebuildController(SolrIndexRebuildService solrIndexRebuildService) {       
        this.solrIndexRebuildService = solrIndexRebuildService;
    }
    
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
