package nz.co.searchwellington.controllers.profiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;
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
	private TagDAO tagDAO;
	private ContentRetrievalService contentRetrievalService;
        
    
	public ProfileController(UserRepository userDAO,
			LoggedInUserFilter loggerInUserFilter,
			ResourceRepository resourceDAO, UrlBuilder urlBuilder, TagDAO tagDAO, ContentRetrievalService contentRetrievalService) {
		this.userDAO = userDAO;
		this.loggerInUserFilter = loggerInUserFilter;
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
		this.tagDAO = tagDAO;
		this.contentRetrievalService = contentRetrievalService;
	}

	
	
	@Transactional
    public ModelAndView all(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("profiles");    
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Profiles");
        mv.addObject("profiles", userDAO.getActiveUsers());        
        return mv;
    }



	@Transactional
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("editProfile");    
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
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
		  return new ModelAndView(new RedirectView(urlBuilder.getProfileUrl(loggedInUser)));
	  }

	
	  @Transactional
	  public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {	        
	        String path = request.getPathInfo();	       
	        if (path.matches("^/profiles/.*$")) {
	        	final String profilename = path.split("/")[2];	        	
		        
		        User user = userDAO.getUserByProfileName(profilename);
	        	if (user != null) {
	        		ModelAndView mv = new ModelAndView("viewProfile");
	        		User loggedInUser = loggerInUserFilter.getLoggedInUser();
	        		if (loggedInUser != null && loggedInUser.getId() == user.getId()) {
	        			mv = new ModelAndView("profile");
	        		}
				
	        		mv.addObject("heading", "User profile");
	        		mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
	        		
	        		log.info("Put user onto model: " + user.getUsername());
	        		mv.addObject("profileuser", user);
	        		
	        		mv.addObject("submitted", resourceDAO.getOwnedBy(user, MAX_NEWSITEMS));	// TODO move to CRS
	        		mv.addObject("tagged", contentRetrievalService.getTaggedBy(user, MAX_NEWSITEMS));
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
