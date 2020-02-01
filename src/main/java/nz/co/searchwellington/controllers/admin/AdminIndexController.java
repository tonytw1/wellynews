package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AdminIndexController {

	private final LoggedInUserFilter loggedInUserFilter;

	@Autowired
	public AdminIndexController(LoggedInUserFilter loggedInUserFilter) {
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	@RequestMapping("/admin")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		final User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null || !loggedInUser.isAdmin()) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	
		final ModelAndView mv = new ModelAndView("adminindex");
		mv.addObject("heading", "Admin index");	
		return mv;
	}

}
