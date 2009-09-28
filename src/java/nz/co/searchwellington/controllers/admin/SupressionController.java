package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.Supression;
import nz.co.searchwellington.model.SupressionImpl;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.repositories.SupressionRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public class SupressionController extends MultiActionController {

    Logger log = Logger.getLogger(SupressionController.class);

    private SupressionRepository supressionDAO;  
    private UrlStack urlStack;
	private SuggestionDAO suggestionDAO;
  

    
    public SupressionController(SupressionRepository supressionDAO, UrlStack urlStack, SuggestionDAO suggestionDAO) {
        this.supressionDAO = supressionDAO;       
        this.urlStack = urlStack;
        this.suggestionDAO = suggestionDAO;
    }

    public ModelAndView supress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
                
        setRedirect(mv, request);        
        if (request.getParameter("url") != null) {
            String urlToSupress = request.getParameter("url");
            Supression supression = new SupressionImpl(urlToSupress);
            
            log.info("Adding a url supression for: " + supression.getUrl());
            if (!supressionDAO.isSupressed(urlToSupress)) {
            	supressionDAO.addSupression(supression);
            }
            // TODO put this somewhere of it's own?
            suggestionDAO.removeSuggestion(urlToSupress);
        }
        return mv;
    }
    
    
    public ModelAndView unsupress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
                
        setRedirect(mv, request);        
        if (request.getParameter("url") != null) {
            String urlToSupress = request.getParameter("url");
            Supression supression = new SupressionImpl(urlToSupress);
            
            log.info("Removing url supression for: " + supression.getUrl());
            supressionDAO.removeSupressionForUrl(supression.getUrl());
        }
        return mv;
    }
   
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
    
}
