package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {
	
    private UrlStack urlStack;

    @Autowired
    public LoginController(UrlStack urlStack) {       
        this.urlStack = urlStack;        
    }
    
    @RequestMapping("/logout")
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
