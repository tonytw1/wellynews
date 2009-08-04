package nz.co.searchwellington.controllers.profiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public class ProfileEditController extends MultiActionController {
    
    Logger log = Logger.getLogger(ProfileEditController.class);

    private UserRepository userDAO;
    private LoggedInUserFilter loggerInUserFilter;
    private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
        
    
	

	public ProfileEditController(UserRepository userDAO,
			LoggedInUserFilter loggerInUserFilter,
			ResourceRepository resourceDAO, UrlBuilder urlBuilder) {
		super();
		this.userDAO = userDAO;
		this.loggerInUserFilter = loggerInUserFilter;
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
	}




	@Transactional
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("editProfile");    
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
        mv.addObject("heading", "Editing your profile");

        User loggedInUser = loggerInUserFilter.getLoggedInUser();
        mv.addObject("user", loggedInUser);
        
        return mv;
    }
	
	
	
	
	  @Transactional(propagation=Propagation.REQUIRED)
	  public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
	      
		  User loggedInUser = loggerInUserFilter.getLoggedInUser();		  
		  if (loggedInUser != null) {
			  loggedInUser.setProfilename(request.getParameter("profilename"));
			  log.info("User profile name set to: " + loggedInUser.getProfilename());
			  loggedInUser.setUrl(request.getParameter("url"));
			  log.info("User url set to: " + loggedInUser.getUrl());
			  userDAO.saveUser(loggedInUser);
		  }
	      
		  return new ModelAndView(new RedirectView(urlBuilder.getProfileUrl()));
	  }

	
	
	
	
	
	
	
	
	
	
	
	
	
        
}
