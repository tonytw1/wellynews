package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.SolrIndexRebuildService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class IndexRebuildController extends BaseMultiActionController {

	Logger log = Logger.getLogger(IndexRebuildController.class);
    
	private SolrIndexRebuildService solrIndexRebuildService;
    private boolean indexingLock;
        
    public IndexRebuildController(SolrIndexRebuildService solrIndexRebuildService) {
        super();
        this.solrIndexRebuildService = solrIndexRebuildService;
        this.indexingLock = false;
    }

    
    @SuppressWarnings("unchecked")
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
                
        mv.setViewName("luceneIndexBuilder");
        if (!indexingLock) {
            indexingLock = true;            
            solrIndexRebuildService.buildIndex();
            mv.getModel().put("message", "Created new index.");
            indexingLock = false;
            
        } else {
            mv.getModel().put("message", "Index is locked; index is already been rebuild.");
        }
        return mv;
    }

}
