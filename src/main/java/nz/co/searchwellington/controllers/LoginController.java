package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public class LoginController extends MultiActionController {
	
    private UrlStack urlStack;

    protected LoginController(UrlStack urlStack) {       
        this.urlStack = urlStack;        
    }
    
    @RequestMapping("/login/logout")	// TODO That's an interesting url
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {        
        final ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("user", null);
        setRedirect(modelAndView, request);   
        return modelAndView;
    }
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
        
}
