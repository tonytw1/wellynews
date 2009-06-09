package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


public class PublisherAjaxController implements Controller {
	
    Logger log = Logger.getLogger(PublisherAjaxController.class);
	private ResourceRepository resourceDAO;

   
    public PublisherAjaxController(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
    
    
	@SuppressWarnings("unchecked")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<String> suggestions = new ArrayList<String>();
        if (request.getParameter("q") != null) {
        	suggestions = getSuggestions(request.getParameter("q"));
        }        	
        mv.getModel().put("suggestions", suggestions);       
        mv.setViewName("autocompleteData");
        return mv;
    }
	
    private List<String> getSuggestions(String q) {
        log.info("Looking up possible publishers starting with: " + q);
        return resourceDAO.getPublisherNamesByStartingLetters(q);    
	}
    
}
    