package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;


public class LoginController extends MultiActionController {
    
    static Logger log = Logger.getLogger(LoginController.class);
    private UrlStack urlStack;

    protected LoginController(UrlStack urlStack) {       
        this.urlStack = urlStack;        
    }
    
    
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("user", null); 
        setRedirect(modelAndView, request);                
        return modelAndView;
    }
    
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
        
}
