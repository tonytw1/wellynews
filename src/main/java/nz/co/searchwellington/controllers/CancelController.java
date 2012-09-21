package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CancelController extends BaseMultiActionController {
                
    public CancelController(UrlStack urlStack) { 
        this.urlStack = urlStack;
    }
    
    @RequestMapping("/cancel")
    public ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) {
    	return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }

}
