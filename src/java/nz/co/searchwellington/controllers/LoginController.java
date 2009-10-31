package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;


public class LoginController extends MultiActionController {
    
    Logger log = Logger.getLogger(LoginController.class);
    private UrlStack urlStack;


    protected LoginController(UrlStack urlStack) {       
        this.urlStack = urlStack;        
    }
   

    public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("login_prompt", "1");
        log.debug("login_prompt set on session");
        setRedirect(modelAndView, request);
                
        return modelAndView;
    }
    
    
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("user", null); 
        request.getSession().setAttribute("login_prompt", null);                        
        setRedirect(modelAndView, request);                
        return modelAndView;
    }
    
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
    
    
}
