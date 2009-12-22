package nz.co.searchwellington.controllers.profiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.UserRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class ProfileController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ProfileController.class);

    private UserRepository userDAO;
    private LoggedInUserFilter loggerInUserFilter;
    private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
        
    
	

	public ProfileController(UserRepository userDAO,
			LoggedInUserFilter loggerInUserFilter,
			ResourceRepository resourceDAO, UrlBuilder urlBuilder) {
	
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
	
	
	
	
	  @Transactional
	  public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {	      
		  User loggedInUser = loggerInUserFilter.getLoggedInUser();		  
		  if (loggedInUser != null) {
			  if (request.getParameter("profilename") != null && isProfilenameValid(request.getParameter("profilename"))) {
				  loggedInUser.setProfilename(request.getParameter("profilename"));
			  }
			  
			  loggedInUser.setName(request.getParameter("name"));			
			  loggedInUser.setBio(request.getParameter("bio"));			  			  
			  loggedInUser.setUrl(request.getParameter("url"));
			  
			  userDAO.saveUser(loggedInUser);
		  }
		  return new ModelAndView(new RedirectView(urlBuilder.getProfileUrl()));
	  }

	
	  




	  @Transactional
	  public ModelAndView profile(HttpServletRequest request, HttpServletResponse response) {
		  User loggedInUser = loggerInUserFilter.getLoggedInUser();
		  log.info("Logged in user is: " + loggedInUser);
		  if (loggedInUser != null)	{
			  if (loggedInUser.getProfilename() == null) {
				  ModelAndView mv = new ModelAndView();
				  mv.addObject("heading", "User profile");
				  mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
				  
				  mv.addObject("user", loggedInUser);
				  mv.addObject("submitted", resourceDAO.getOwnedBy(loggedInUser, MAX_NEWSITEMS));
				  mv.setViewName("profile");
				  return mv;
			  } else {
	        	 return new ModelAndView(new RedirectView(urlBuilder.getProfileUrl(loggedInUser)));
			  }      
		  }
		  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		  return null;
	  }
	    
	  	  
	  @Transactional
	  public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {	        
	        String path = request.getPathInfo();	       
	        if (path.matches("^/profile/.*$")) {
	        	final String profilename = path.split("/")[2];	        	
		        
		        User user = userDAO.getUserByProfileName(profilename);
	        	if (user != null) {
	        		ModelAndView mv = new ModelAndView("viewProfile");
	        		User loggedInUser = loggerInUserFilter.getLoggedInUser();
	        		if (loggedInUser != null && loggedInUser.getId() == user.getId()) {
	        			mv = new ModelAndView("profile");
	        		}
				
	        		mv.addObject("heading", "User profile");
	        		mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
	        		
	        		log.info("Put user onto model: " + user.getUsername());
	        		mv.addObject("profileuser", user);
	        		mv.addObject("submitted", resourceDAO.getOwnedBy(user, MAX_NEWSITEMS));
	        		return mv;
	        	}
	        }
	        	
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
	  }
	  
	  
	  private boolean isProfilenameValid(String profilename) {
		  if (profilename.matches("[a-z|A-Z|0-9]+")) {
			  if (userDAO.getUserByProfileName(profilename) == null) {
				  return true;
			  }
		  }		  
		  return false;
	  }	
	    
}
