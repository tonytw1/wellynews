package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.SupressionService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public class SupressionController extends MultiActionController {

    private static Logger log = Logger.getLogger(SupressionController.class);

    private SupressionService suppressionService;
    private UrlStack urlStack;
	private LoggedInUserFilter loggedInUserFilter;

	
    protected SupressionController(SupressionService suppressionService, UrlStack urlStack, LoggedInUserFilter loggedInUserFilter) {		
		this.suppressionService = suppressionService;
		this.urlStack = urlStack;
		this.loggedInUserFilter = loggedInUserFilter;
	}


	public ModelAndView supress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        setRedirect(mv, request);        
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();;        
        if (loggedInUser != null && request.getParameter("url") != null) {
            String urlToSupress = request.getParameter("url");
            suppressionService.suppressUrl(urlToSupress);                       
        }
        return mv;
    }
    
    
    public ModelAndView unsupress(HttpServletRequest request, HttpServletResponse response) throws IOException {ModelAndView mv = new ModelAndView();                
        setRedirect(mv, request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        if (loggedInUser != null && request.getParameter("url") != null) {        	
           suppressionService.unsupressUrl(request.getParameter("url"));
        }
        return mv;
    }
   
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
    
}
