package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public abstract class BaseAjaxController implements Controller {
	    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<String> suggestions = new ArrayList<String>();
        if (request.getParameter("q") != null) {
        	suggestions = this.getSuggestions(request.getParameter("q"));
        }        	
        mv.addObject("suggestions", suggestions);       
        mv.setViewName("autocompleteData");	// TODO migrate to JSON view?
        return mv;
    }
    
    protected abstract List<String> getSuggestions(String q);	
    
}
    