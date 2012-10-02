package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.SupressionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SupressionController {
	
    private SupressionService suppressionService;
    private UrlStack urlStack;
	private LoggedInUserFilter loggedInUserFilter;

	public SupressionController() {
	}
	
	@Autowired
    protected SupressionController(SupressionService suppressionService, UrlStack urlStack, LoggedInUserFilter loggedInUserFilter) {		
		this.suppressionService = suppressionService;
		this.urlStack = urlStack;
		this.loggedInUserFilter = loggedInUserFilter;
	}
    
	@RequestMapping("/supress/supress")
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
	
	@RequestMapping("/supress/unsupress")
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
