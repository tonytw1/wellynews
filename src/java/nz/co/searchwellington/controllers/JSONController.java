package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.views.JSONView;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class JSONController extends MultiActionController {

	Logger log = Logger.getLogger(JSONController.class);
    
    
    private RequestFilter requestFilter;
	private ContentModelBuilderService contentModelBuilderService;

       
    public JSONController(RequestFilter requestFilter, ContentModelBuilderService contentModelBuilderService) {        
        this.requestFilter = requestFilter;       
        this.contentModelBuilderService = contentModelBuilderService;
    }
       
	public ModelAndView contentJSON(HttpServletRequest request, HttpServletResponse response) throws Exception {    
    	log.info("Building content json");
    	 requestFilter.loadAttributesOntoRequest(request);  
         ModelAndView mv = contentModelBuilderService.populateContentModel(request);
         mv.setView(new JSONView());
         return mv;
    }
    
}
