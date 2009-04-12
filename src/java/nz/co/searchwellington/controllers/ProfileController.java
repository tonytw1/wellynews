package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class ProfileController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ProfileController.class);
    
    private SiteInformation siteInformation;
     
    public ProfileController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, SiteInformation siteInformation) {       
        this.resourceDAO = resourceDAO;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;
    }
    
       
    public ModelAndView profile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());        
                        
        mv.addObject("heading", "Profile");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        mv.setViewName("profile");
        return mv;
    }


        
   
       
    

}
    