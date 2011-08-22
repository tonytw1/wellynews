package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

@Deprecated
public class JSONController extends MultiActionController {

	private static Logger log = Logger.getLogger(JSONController.class);

	private static final String VALID_CALLBACK_NAME_REGEX = "[a-z|A-Z|0-9|_]+";
        
	private ContentModelBuilderService contentModelBuilderService;
       
    public JSONController(ContentModelBuilderService contentModelBuilderService) {        
        this.contentModelBuilderService = contentModelBuilderService;
    }
   
    public ModelAndView contentJSON(HttpServletRequest request, HttpServletResponse response) throws Exception {    
		log.info("Building content json");
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);
        		
		if (mv != null) {			
			if(request.getParameter("callback") != null) {
				final String callback = request.getParameter("callback");
				if (isValidCallbackName(callback)) {
					log.info("Adding callback to model:" + callback);
					mv.addObject("callback", callback);
				}	 
			}			
			return mv;
		}     
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;         
    }
    
	protected boolean isValidCallbackName(String callback) {
		return callback.matches(VALID_CALLBACK_NAME_REGEX);
	}
    
}
