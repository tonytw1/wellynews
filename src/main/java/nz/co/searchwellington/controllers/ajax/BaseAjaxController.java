package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;

public abstract class BaseAjaxController {
	    
    private static final String TERM = "term";
    
    protected ViewFactory viewFactory;

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        List<String> suggestions = Collections.emptyList();
        if (request.getParameter(TERM) != null) {
        	suggestions = this.getSuggestions(request.getParameter(TERM));
        }
        mv.addObject("data", suggestions);
        return mv;
    }
    
    protected abstract List<String> getSuggestions(String q);	
    
}
