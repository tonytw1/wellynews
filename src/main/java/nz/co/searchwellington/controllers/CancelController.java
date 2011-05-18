package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


public class CancelController extends BaseMultiActionController {

                
    public CancelController(UrlStack urlStack) { 
        this.urlStack = urlStack;
    }
    
    
    
    public ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();        
        String url = urlStack.getExitUrlFromStack(request);                
        mv.setView(new RedirectView(url));     
        return mv;
    }

}
    