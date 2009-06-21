package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;


public class LoginController extends MultiActionController {
    
    Logger log = Logger.getLogger(LoginController.class);
      
    private UserRepository userDAO;
    private UrlStack urlStack;


    protected LoginController(UserRepository userDAO, UrlStack urlStack) {
        this.userDAO = userDAO;
        this.urlStack = urlStack;        
    }
   

    public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("login_prompt", "1");
        log.debug("login_prompt set on session");
        setRedirect(modelAndView, request);
                
        return modelAndView;
    }


    
    
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {     
        ModelAndView modelAndView = new ModelAndView();
        
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");
        
        if (username != null && password != null) {
            log.info("Unsetting login_prompt.");
            request.getSession().setAttribute("login_prompt", null);
            log.info("login_prompt is: " + request.getSession().getAttribute("login_prompt"));
            
            User testUser = userDAO.getUser(username, password);
            if (testUser != null) {
                request.getSession().setAttribute("user", testUser);
                log.info("Logged in user: " + testUser.getUsername());
            }
            
            
        } else {        
            request.getSession().setAttribute("user", null); 
            request.getSession().setAttribute("login_prompt", null);
        }
        
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
