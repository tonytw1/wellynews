package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.LuceneBackedResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class LuceneIndexBuilderController extends BaseMultiActionController {

	Logger log = Logger.getLogger(LuceneIndexBuilderController.class);
    
    private LuceneBackedResourceDAO luceneResourceDAO;
    private boolean indexingLock;
        
    public LuceneIndexBuilderController(LuceneBackedResourceDAO luceneResourceDAO) {
        super();
        this.luceneResourceDAO = luceneResourceDAO;
        this.indexingLock = false;
    }




    @SuppressWarnings("unchecked")
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
                
        mv.setViewName("luceneIndexBuilder");      
        mv.getModel().put("index_path", luceneResourceDAO.getIndexPath());
        
        if (!indexingLock) {
            indexingLock = true;
            
            try {
            	luceneResourceDAO.buildIndex();            
            	mv.getModel().put("message", "Created new index.");
            	indexingLock = false;
            } catch (IOException e) {
				log.error("Failed to build index.", e);
				indexingLock = false;				
			}
            
        } else {
            mv.getModel().put("message", "Index is locked; index is already been rebuild.");
        }
        return mv;
    }



    
    
    
    
}
