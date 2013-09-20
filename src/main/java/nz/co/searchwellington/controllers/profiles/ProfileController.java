package nz.co.searchwellington.controllers.profiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.annotations.Timed;
import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ProfileController {
    
	private static Logger log = Logger.getLogger(ProfileController.class);
    
    private HibernateBackedUserDAO userDAO;
    private LoggedInUserFilter loggerInUserFilter;
	private UrlBuilder urlBuilder;
	private ContentRetrievalService contentRetrievalService;
	private CommonModelObjectsService commonModelObjectsService;
	
	public ProfileController() {
	}
	
	@Autowired
	public ProfileController(HibernateBackedUserDAO userDAO,
			LoggedInUserFilter loggerInUserFilter, UrlBuilder urlBuilder,
			ContentRetrievalService contentRetrievalService,
			CommonModelObjectsService commonModelObjectsService) {
		this.userDAO = userDAO;
		this.loggerInUserFilter = loggerInUserFilter;
		this.urlBuilder = urlBuilder;
		this.contentRetrievalService = contentRetrievalService;
		this.commonModelObjectsService = commonModelObjectsService;
	}
	
	@Transactional
	@RequestMapping("/profiles")
    @Timed(timingNotes = "")
    public ModelAndView all(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("profiles");    
        mv.addObject("heading", "Profiles");
        commonModelObjectsService.populateCommonLocal(mv);
        mv.addObject("profiles", userDAO.getActiveUsers());        
        return mv;
    }
	
	@Transactional
	@RequestMapping("/profile/edit")
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("editProfile");    
        commonModelObjectsService.populateCommonLocal(mv);
        mv.addObject("heading", "Editing your profile");

        User loggedInUser = loggerInUserFilter.getLoggedInUser();
        mv.addObject("user", loggedInUser);
        
        return mv;
    }	
	  
	@Transactional
	@RequestMapping(value="/profile/edit", method=RequestMethod.POST)
	public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {	      
		final User loggedInUser = loggerInUserFilter.getLoggedInUser();		  
		if (loggedInUser != null) {
			if (request.getParameter("profilename") != null && isValidNewProfilename(request.getParameter("profilename"))) {
				loggedInUser.setProfilename(request.getParameter("profilename"));
			}
			  
			loggedInUser.setName(request.getParameter("name"));			
			loggedInUser.setBio(request.getParameter("bio"));			  			  
			loggedInUser.setUrl(request.getParameter("url"));			  
			userDAO.saveUser(loggedInUser);
		  	}
		  
		return new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(loggedInUser.getProfilename())));
	}
	
	@Transactional(readOnly=true)
	@RequestMapping("/profiles/*")
    @Timed(timingNotes = "")
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		final String path = request.getPathInfo();
		if (path.matches("^/profiles/.*$")) {
			final String profilename = path.split("/")[2];	// TODO move to spring path parameters
			final User user = userDAO.getUserByProfileName(profilename);
			if (user != null) {
				log.debug("Rendering profile for user: "+ profilename);
				ModelAndView mv = new ModelAndView("viewProfile");
				User loggedInUser = loggerInUserFilter.getLoggedInUser();
				if (loggedInUser != null && loggedInUser.getId() == user.getId()) {
					mv = new ModelAndView("profile");
				}
				
				mv.addObject("heading", "User profile");
				commonModelObjectsService.populateCommonLocal(mv);
				mv.addObject("profileuser", user);

				mv.addObject("submitted", contentRetrievalService.getOwnedBy(user));
				mv.addObject("tagged", contentRetrievalService.getTaggedBy(user));
				return mv;
			}
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	protected boolean isValidNewProfilename(String profilename) {
		if (profilename.matches("[a-z|A-Z|0-9]+")) {
			if (userDAO.getUserByProfileName(profilename) == null) {
				return true;
			}
		}
		return false;
	}
	
}
